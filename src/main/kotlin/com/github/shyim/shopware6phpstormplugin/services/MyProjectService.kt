package com.github.shyim.shopware6phpstormplugin.services

import com.intellij.openapi.project.Project
import com.github.shyim.shopware6phpstormplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
