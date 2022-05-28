package de.shyim.shopware6.action.generator

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import de.shyim.shopware6.templates.ShopwareTemplates

class NewConfigXmlAction : DumbAwareAction("Create a Config.Xml", "Create a new config.xml", AllIcons.FileTypes.Xml) {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project? = e.getData(PlatformDataKeys.PROJECT)

        if (project === null) {
            return
        }

        ActionUtil.buildFile(
            e,
            project,
            ShopwareTemplates.renderTemplate(project, ShopwareTemplates.SHOPWARE_PLUGIN_CONFIG_TEMPLATE, null),
            "config.xml",
            XmlFileType.INSTANCE
        )
    }

}