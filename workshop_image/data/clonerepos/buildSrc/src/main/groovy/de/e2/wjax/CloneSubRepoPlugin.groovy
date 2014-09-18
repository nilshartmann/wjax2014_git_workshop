package de.e2.wjax

import org.gradle.api.Plugin
import org.gradle.api.Project

class CloneSubRepoPlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        if(!project.hasProperty('repoDirRoot')) {
            project.ext.repoDirRoot='repo_clones'
        }

        project.tasks.withType(CloneSubRepoTask) {
            conventionMapping.repoUser = {
                if (project.hasProperty('repoUser')) return project.repoUser
                return null
            }
            conventionMapping.repoPassword = {
                if (project.hasProperty('repoPassword')) return project.repoPassword
                return null
            }
        }
    }
}
