package de.shyim.shopware6.action.generator.php

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.roots.PhpNamespaceByFilesProvider
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.templates.ShopwareTemplates.Companion.SHOPWARE_PHP_SCHEDULED_TASK
import de.shyim.shopware6.templates.ShopwareTemplates.Companion.SHOPWARE_PHP_SCHEDULED_TASK_HANDLER
import icons.ShopwareToolBoxIcons

class NewScheduledTaskAction :
    DumbAwareAction("Create Scheduled Task", "Create a new ScheduledTask ", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val directory = ActionUtil.getViewDirectory(e.dataContext) ?: return

        val namespaces = PhpNamespaceByFilesProvider.INSTANCE.suggestNamespaces(directory)
        val namespace = namespaces.getOrNull(0) ?: ""

        val ui = NewScheduledTaskDialogWrapper(namespace)
        val config = ui.showAndGetConfig() ?: return

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.renderTemplate(e.project!!, SHOPWARE_PHP_SCHEDULED_TASK, config.toMap()),
            config.name + "Task.php",
            PhpFileType.INSTANCE
        )

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.renderTemplate(e.project!!, SHOPWARE_PHP_SCHEDULED_TASK_HANDLER, config.toMap()),
            config.name + "TaskHandler.php",
            PhpFileType.INSTANCE
        )
    }
}