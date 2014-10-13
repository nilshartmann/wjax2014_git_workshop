package de.e2.mergetool;


import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.junit.RepositoryTestCase;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MergeToolTest extends RepositoryTestCase{

    private Git prepareGit;
    private Git bareGit;
    private MergeTool mergeTool;

    @Before
    public void initGit() throws IOException, GitAPIException {
        prepareGit =new Git(db);
        bareGit=new Git(createBareRepository());
        String bareUrl = bareGit.getRepository().getDirectory().getAbsolutePath();

        StoredConfig config = prepareGit.getRepository().getConfig();
        config.setString("remote", "origin", "url", bareUrl);
        config.save();

        commitFile("common.txt", "common", "master");
        commitFile("int.txt","int","int");
        commitFile("master.txt","master","master");

        prepareGit.push().add("master").add("int").setRemote("origin").call();


        File workDir = createTempDirectory("workDir");
        mergeTool=new MergeTool(workDir.getAbsolutePath(),bareGit.getRepository().getDirectory().getAbsolutePath(),"int","master");
        mergeTool.prepareAndValidateRepo();
        db= (FileRepository) mergeTool.git.getRepository();
    }

    @Test
    public void testClean() throws IOException, GitAPIException {

        writeTrashFile("new.txt","new");
        Assert.assertFalse(mergeTool.git.status().call().getUntracked().isEmpty());

        File tempDir = new File(db.getWorkTree(), "tempDir");
        tempDir.mkdirs();
        Assert.assertTrue(tempDir.exists());

        mergeTool.cleanRepo();

        Status status = mergeTool.git.status().call();
        Assert.assertTrue(status.getUntracked().isEmpty());
        Assert.assertFalse(tempDir.exists());
    }

    @Test
    public void testFetchAllBranches() throws IOException, GitAPIException {
        db=(FileRepository) prepareGit.getRepository();
        commitFile("int2.txt","int","int");
        commitFile("master2.txt","master","master");
        prepareGit.push().add("master").add("int").setRemote("origin").call();

        mergeTool.fetchAllBranches();

        Assert.assertEquals(prepareGit.getRepository().resolve("master"), mergeTool.git.getRepository().resolve("origin/master"));
        Assert.assertEquals(prepareGit.getRepository().resolve("int"), mergeTool.git.getRepository().resolve("origin/int"));
    }

    @Test
    public void testResetLocalDestBranchToRemoteDestBranch() throws IOException, GitAPIException {
        commitFile("common.txt","master","master");
        commitFile("master2.txt","master","master");
        File master2File = new File(db.getWorkTree(), "master2.txt");
        Assert.assertTrue(master2File.exists());
        Assert.assertNotEquals(db.resolve("master"), db.resolve("origin/master"));
        mergeTool.git.branchCreate().setName("int").setStartPoint("origin/int").call();
        checkoutBranch("refs/heads/int");
        writeTrashFile("common.txt","int");
        Assert.assertTrue(mergeTool.git.status().call().hasUncommittedChanges());

        mergeTool.resetLocalDestBranchToRemoteDestBranch();

        Assert.assertFalse(mergeTool.git.status().call().hasUncommittedChanges());
        Assert.assertFalse(master2File.exists());
        Assert.assertEquals(db.resolve("master"), db.resolve("origin/master"));
    }

    @Test
    public void testMergeRemoteSrcBranchIntoLocalDestBranchSuccessful() throws IOException, GitAPIException {
        File intFile = new File(db.getWorkTree(), "int.txt");
        Assert.assertFalse(intFile.exists());
        Assert.assertEquals(db.resolve("master"),db.resolve("origin/master"));

        int commitsBeforeCount=0;
        for (RevCommit revCommit : mergeTool.git.log().addRange(db.resolve("master"), db.resolve("origin/int")).call()) {
            commitsBeforeCount++;
        }
        Assert.assertEquals(1,commitsBeforeCount);

        boolean success=mergeTool.mergeRemoteSrcBranchIntoLocalDestBranch();
        Assert.assertTrue(success);

        int commitsAfterCount=0;
        for (RevCommit revCommit : mergeTool.git.log().addRange(db.resolve("master"), db.resolve("origin/int")).call()) {
            commitsAfterCount++;
        }
        Assert.assertEquals(0,commitsAfterCount);

        Assert.assertTrue(intFile.exists());
        Assert.assertNotEquals(db.resolve("master"),db.resolve("origin/master"));
    }

    @Test
    public void testMergeRemoteSrcBranchIntoLocalDestBranchConflict() throws IOException, GitAPIException {
        db=(FileRepository) prepareGit.getRepository();
        commitFile("common.txt","int","int");
        prepareGit.push().add("int").setRemote("origin").call();
        mergeTool.fetchAllBranches();
        db=(FileRepository) mergeTool.git.getRepository();
        commitFile("common.txt","master","master");

        boolean success=mergeTool.mergeRemoteSrcBranchIntoLocalDestBranch();
        Assert.assertFalse(success);
    }

}
