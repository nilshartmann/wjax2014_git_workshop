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
  @Input @Optional
  String prefix=''
  @Input @Optional
  String defaultBranch='master'

  @TaskAction
  void run() {
    def repoDir=new File(targetDir)
    if(deleteTargetDir) project.delete repoDir

    def repo = Grgit.init(dir: repoDir)

    StoredConfig config = repo.repository.jgit.getRepository().getConfig()
    config.setString("remote", "origin", "url", repoUrl)
    config.setString("remote", "origin", "fetch", "+refs/heads/$prefix*:refs/remotes/origin/*")
    config.save()

    repo.fetch(remote: "origin")
    repo.branch.add(name: defaultBranch, startPoint: "origin/$defaultBranch", mode: BranchAddOp.Mode.TRACK)
    repo.checkout(branch: defaultBranch)
    //The checkout in an empty repo does not change the workspace
    repo.reset(commit: 'HEAD', mode: ResetOp.Mode.HARD)

    repo.close()
  }
}
