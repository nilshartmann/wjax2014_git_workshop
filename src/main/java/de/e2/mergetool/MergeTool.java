package de.e2.mergetool;


import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class MergeTool implements Closeable{
    private final File repoDir;
    private final String repoUrl;
    private final String localSrcBranch;
    private final String localDestBranch;
    private String remoteDestBranch;
    private String remoteSrcBranch;
    protected Git git;

    public MergeTool(String workDir, String repoUrl, String srcBranch, String destBranch) {
        this.repoUrl = repoUrl;
        this.localSrcBranch = srcBranch;
        this.localDestBranch = destBranch;
        this.remoteSrcBranch = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/" + srcBranch;
        this.remoteDestBranch = Constants.R_REMOTES + Constants.DEFAULT_REMOTE_NAME + "/" + destBranch;


        try {
            URIish urIish=new URIish(repoUrl);
            String humanishName = urIish.getHumanishName();
            if(humanishName==null || humanishName.isEmpty()) {
                throw new IllegalArgumentException("Can't determine RepoUrl from local name");
            }

            this.repoDir =new File(workDir,humanishName);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public void run() throws GitAPIException,IOException {
        prepareAndValidateRepo();

        int retries=3;

        while(--retries>0) {
            fetchAllBranches();
            resetLocalDestBranchToRemoteDestBranch();
            cleanRepo();
            if(!mergeRemoteSrcBranchIntoLocalDestBranch()) {
                throw new IllegalStateException("Could not merge branches");
            }
            if(pushLocalDestBranch()) {
                return;
            }
        }

        throw new IllegalStateException("Could not push branch");
    }

    protected void prepareAndValidateRepo() throws IOException, GitAPIException {
        if(!new File(repoDir, Constants.DOT_GIT).exists()){
            cloneRepo();
        } else {
            Repository repo = new FileRepositoryBuilder().setWorkTree(repoDir).setup().build();
            git=new Git(repo);
        }

        Ref remoteDestBranchRef = git.getRepository().getRef(remoteDestBranch);
        if(remoteDestBranchRef==null){
            throw new IllegalStateException("Remote-Dest-Branch does not exists: " + remoteDestBranch);
        }
        Ref remoteSrcBranchRef = git.getRepository().getRef(remoteSrcBranch);
        if(remoteSrcBranchRef==null){
            throw new IllegalStateException("Remote-Src-Branch does not exists: " + remoteSrcBranch);
        }
        Ref localDestBranchRef = git.getRepository().getRef(localDestBranch);
        if(localDestBranchRef==null) {
            git.branchCreate().setName(localDestBranch).setStartPoint(remoteDestBranch).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM).call();
        }
    }

    private void cloneRepo() throws GitAPIException {
        git = Git.cloneRepository().setDirectory(repoDir).setURI(repoUrl).call();
    }

    void cleanRepo() throws GitAPIException {
        //TODO clean the repo from untracked files and directories
    }

    void fetchAllBranches() throws IOException, GitAPIException {
        //TODO Fetch all branches
    }

    void resetLocalDestBranchToRemoteDestBranch() throws IOException, GitAPIException {
        //TODO Reset the local destination Branch, e.g. master to the remote destination Branch, e.g. origin/master
    }

    boolean mergeRemoteSrcBranchIntoLocalDestBranch() throws IOException, GitAPIException {
        //TODO Merge the remote source Branch, e.g. int into the local destination branch, e.g. master
        //TODO return true if the merge was successful
        return false;
    }

    private boolean pushLocalDestBranch() throws GitAPIException {
        Iterable<PushResult> pushResults = git.push().setRefSpecs(new RefSpec(localDestBranch)).call();
        for (PushResult pushResult : pushResults) {
            for (RemoteRefUpdate remoteRefUpdate : pushResult.getRemoteUpdates()) {
                if(remoteRefUpdate.getStatus()== RemoteRefUpdate.Status.OK || remoteRefUpdate.getStatus()== RemoteRefUpdate.Status.UP_TO_DATE) {
                    return true;
                }
            }
        }
        return false;

    }



    @Override
    public void close()  {
        if (git != null) {
            git.close();
            git=null;
        }
    }
}
