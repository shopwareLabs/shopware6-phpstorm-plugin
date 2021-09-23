package de.shyim.shopware6.action.generator.php

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.roots.PhpNamespaceByFilesProvider
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates
import icons.ShopwareToolBoxIcons

class NewScheduledTaskAction :
    DumbAwareAction("Create scheduled task", "Create a new ScheduledTask ", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val directory = ActionUtil.getViewDirectory(e.dataContext) ?: return

        val namespaces = PhpNamespaceByFilesProvider.INSTANCE.suggestNamespaces(directory)
        val namespace = namespaces.getOrNull(0).toString()

        val ui = NewScheduledTaskDialogWrapper(namespace)
        val config = ui.showAndGetConfig() ?: return

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.applyShopwarePHPScheduledTask(e.project!!, config),
            config.name + "Task.php",
            PhpFileType.INSTANCE
        )

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.applyShopwarePHPScheduledTaskHandler(e.project!!, config),
            config.name + "TaskHandler.php",
            PhpFileType.INSTANCE
        )
    }
}