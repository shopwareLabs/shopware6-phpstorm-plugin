package com.github.shyim.shopware6phpstormplugin.action.generator

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project

class NewConfigXmlAction() : DumbAwareAction("Create a config.xml", "Create a new config.xml", AllIcons.FileTypes.Xml) {

    override fun actionPerformed(e: AnActionEvent) {
        val project : Project? = e.getData(PlatformDataKeys.PROJECT);

        if (project === null) {
            return
        }

        Util.buildFile(
                e,
                project,
                "/fileTemplates/config.xml",
                "config.xml",
                XmlFileType.INSTANCE
        );
    }

}