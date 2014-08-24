package de.e2.wjax

import org.gradle.api.*
import org.gradle.api.tasks.*
import org.ajoberstar.grgit.*
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.api.*
import org.ajoberstar.grgit.operation.*

class CloneSubRepo extends DefaultTask {
    @Input
    Boolean deleteTargetDir
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

    @Input
    @Optional
    String repoUser

    @Input
    @Optional
    String repoPassword

    String getRepoUser() {
        if (repoUser) return repoUser

        if (project.hasProperty('repoUser')) return project.repoUser

        return null
    }

    String getRepoPassword() {
        if (repoPassword) return repoPassword

        if (project.hasProperty('repoPassword')) return project.repoPassword

        return null
    }

    @TaskAction
    void run() {
        def repoDir = null

        if (targetDir.startsWith('/') || !project.hasProperty('targetDirRoot')) {
            repoDir = project.file(targetDir)
        } else {
            repoDir = new File(new File(project.targetDirRoot), targetDir)
        }


        if (deleteTargetDir) project.delete repoDir

        def repo = Grgit.init(dir: repoDir)

        StoredConfig config = repo.repository.jgit.getRepository().getConfig()
        config.setString("remote", "origin", "url", repoUrl)
        config.setString("remote", "origin", "fetch", "+refs/heads/$prefix*:refs/remotes/origin/*")
        config.save()

        if (getRepoUser() && getRepoPassword()) {
            logger.info('Fetch with user: ' + getRepoUser())
            System.properties.'org.ajoberstar.grgit.auth.username' = getRepoUser()
            System.properties.'org.ajoberstar.grgit.auth.password' = getRepoPassword()
        }

        repo.fetch(remote: "origin")

        System.properties.'org.ajoberstar.grgit.auth.username' = ''
        System.properties.'org.ajoberstar.grgit.auth.password' = ''

        repo.branch.add(name: defaultBranch, startPoint: "origin/$defaultBranch", mode: BranchAddOp.Mode.TRACK)
        repo.checkout(branch: defaultBranch)
        //The checkout in an empty repo does not change the workspace
        repo.reset(commit: 'HEAD', mode: ResetOp.Mode.HARD)

        repo.close()
    }
}
