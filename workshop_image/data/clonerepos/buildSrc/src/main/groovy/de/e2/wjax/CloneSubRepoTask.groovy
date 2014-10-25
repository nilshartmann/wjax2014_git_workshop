package de.e2.wjax

import org.gradle.StartParameter
import org.gradle.api.*
import org.gradle.api.tasks.*
import org.ajoberstar.grgit.*
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.api.*
import org.ajoberstar.grgit.operation.*
import org.gradle.initialization.BuildCancellationToken
import org.gradle.initialization.GradleLauncher
import org.gradle.initialization.GradleLauncherFactory
import org.gradle.process.internal.ExecActionFactory

class CloneSubRepoTask extends DefaultTask {
    @Optional
    @Input
    Boolean deleteTargetDir = true

    @Optional
    @Input
    String targetDir
    @Input
    String repoUrl
    @Input
    @Optional
    String prefix = ''
    @Input
    @Optional
    String defaultBranch = 'master'

    @Optional
    @Input
    Boolean withRemoteBranches = false

    @Input
    @Optional
    Boolean cloneBare

    @Input
    @Optional
    String repoUser

    @Input
    @Optional
    String repoPassword

    @Input
    @Optional
    String runGradleTask

    @Input
    @Optional
    String runMavenTask


    File getRepoDir() {
        return new File(new File(project.repoDirRoot), targetDir)
    }

    @TaskAction
    void run() {
        if(!targetDir) {
            targetDir=name;
        }

        if(cloneBare==null) {
            cloneBare = targetDir.endsWith(".git")
        }
        if(cloneBare && !targetDir.endsWith(".git")) {
            targetDir+=".git"
        }

        def repoDir = getRepoDir()
        repoDir.parentFile.mkdirs()

        if (deleteTargetDir) project.delete repoDir

        def repo = Grgit.init(dir: repoDir, bare: cloneBare)

        StoredConfig config = repo.repository.jgit.getRepository().getConfig()
        config.setString("remote", "origin", "url", repoUrl)
        if(cloneBare || !withRemoteBranches)
        {
            config.setStringList("remote", "origin", "fetch", ["+refs/heads/$prefix*:refs/heads/*".toString(),"+refs/tags/$prefix*:refs/tags/*".toString()])
            config.setStringList("remote", "origin", "push", ["refs/heads/*:refs/heads/$prefix*".toString(),"refs/tags/*:refs/tags/$prefix*".toString()])
        } else {
            config.setStringList("remote", "origin", "fetch", ["+refs/heads/$prefix*:refs/remotes/origin/*".toString(),"+refs/tags/$prefix*:refs/tags/$prefix*".toString()])
            config.setStringList("remote", "origin", "push", ["refs/heads/*:refs/heads/$prefix*".toString(),"refs/tags/*:refs/tags/$prefix*".toString()])
        }
        config.save()

        if (getRepoUser() && getRepoPassword()) {
            logger.info('Fetch with user: ' + getRepoUser())
            System.properties.'org.ajoberstar.grgit.auth.username' = getRepoUser()
            System.properties.'org.ajoberstar.grgit.auth.password' = getRepoPassword()
        }

        repo.fetch(remote: "origin",tagMode: FetchOp.TagMode.NONE)

        System.properties.'org.ajoberstar.grgit.auth.username' = ''
        System.properties.'org.ajoberstar.grgit.auth.password' = ''

        if(!cloneBare) {
            if(withRemoteBranches) {
              repo.branch.add(name: defaultBranch, startPoint: "origin/$defaultBranch", mode: BranchAddOp.Mode.TRACK)
            } else {
                config.unsetSection("remote","origin")
                config.save()
            }

            repo.checkout(branch: defaultBranch)
            //The checkout in an empty repo does not change the workspace
            repo.reset(commit: 'HEAD', mode: ResetOp.Mode.HARD)
        }
        repo.close()

        if(runGradleTask) {
            def execActionFactory = getServices().get(ExecActionFactory.class)
            def execAction = execActionFactory.newExecAction()
            execAction.setWorkingDir(repoDir)
            execAction.setCommandLine("./gradlew",runGradleTask)
            execAction.setIgnoreExitValue(true)
            execAction.execute()

//            def gradleLauncherFactory = getServices().get(GradleLauncherFactory.class)
//            def startParameter = getServices().get(StartParameter.class).newBuild()
//            def cancellationToken = getServices().get(BuildCancellationToken.class);
//            startParameter.setCurrentDir(repoDir)
//            startParameter.setTaskNames([runGradleTask])
//            GradleLauncher launcher = gradleLauncherFactory.newInstance(startParameter,cancellationToken);
//            try {
//                launcher.run()
//            } finally {
//                launcher.stop()
//            }
        }

        if(runMavenTask) {
            def execActionFactory = getServices().get(ExecActionFactory.class)
            def execAction = execActionFactory.newExecAction()
            execAction.setWorkingDir(repoDir)
            execAction.setCommandLine("mvn",runMavenTask)
            execAction.setIgnoreExitValue(true)
            execAction.execute()
        }
    }
}
