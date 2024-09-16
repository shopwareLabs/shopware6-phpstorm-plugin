package de.shyim.shopware6.action.generator.php

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.roots.PhpNamespaceCompositeProvider
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates
import icons.ShopwareToolBoxIcons

class NewMigrationAction :
    DumbAwareAction("Create a Migration", "Create a new Migration", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val directory = ActionUtil.getViewDirectory(e.dataContext) ?: return

        val namespaces = PhpNamespaceCompositeProvider.INSTANCE.suggestNamespaces(directory)
        val namespace = namespaces.getOrNull(0) ?: ""

        val wrapper = NewMigrationDialogWrapper(namespace)
        val config = wrapper.showAndGetConfig() ?: return

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.renderTemplate(e.project!!, ShopwareTemplates.SHOPWARE_PHP_MIGRATION, config.toMap()),
            config.fileName(),
            PhpFileType.INSTANCE
        )
    }
}