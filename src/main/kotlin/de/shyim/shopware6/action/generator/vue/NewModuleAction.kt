package de.shyim.shopware6.action.generator.vue

import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates

class NewModuleAction: DumbAwareAction("Create a module", "Create a new Shopware Admin Module", AllIcons.FileTypes.JavaScript) {
    override fun actionPerformed(e: AnActionEvent) {
        val ui = NewModuleDialogWrapper()
        val config = ui.showAndGetConfig() ?: return

        // Create folder

        val folder = ActionUtil.getViewDirectory(e.dataContext) ?: return

        if (folder.findSubdirectory(config.name) != null) {
            Messages.showInfoMessage("Module already exists", "Error")
            return
        }

        var componentFolder: PsiDirectory? = null
        ApplicationManager.getApplication().runWriteAction {
            componentFolder = folder.createSubdirectory(config.name)
        }

        if (componentFolder == null) {
            return
        }

        // Create module root file

        val content = ShopwareTemplates.applyShopwareAdminVueModule(
            e.project!!,
            config
        )

        val factory = PsiFileFactory.getInstance(e.project)
        val file = factory.createFileFromText("index.js", JavaScriptFileType.INSTANCE, content)

        ApplicationManager.getApplication().runWriteAction {
            CodeStyleManager.getInstance(e.project!!).reformat(file)
            componentFolder!!.add(file)
        }
    }
}