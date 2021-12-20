package de.shyim.shopware6.action.generator.app

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates
import icons.ShopwareToolBoxIcons

class NewAppAction: DumbAwareAction("Create a App", "Create a new Shopware app", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val rootDirectory = ActionUtil.getViewDirectory(e.dataContext) ?: return

        val wrapper = NewAppDialogWrapper()
        val config = wrapper.showAndGetName() ?: return

        if (rootDirectory.findSubdirectory(config.name) != null) {
            Messages.showInfoMessage("App already exists", "Error")
            return
        }

        val appFolder = ActionUtil.createDirectory(rootDirectory, config.name) ?: return

        // Create manifest.xml
        val content = ShopwareTemplates.renderTemplate(
            e.project!!,
            ShopwareTemplates.SHOPWARE_APP_MANIFEST,
            config.toMap()
        )

        ActionUtil.createFile(
            e.project!!,
            XmlFileType.INSTANCE,
            "manifest.xml",
            content,
            appFolder
        )
    }
}