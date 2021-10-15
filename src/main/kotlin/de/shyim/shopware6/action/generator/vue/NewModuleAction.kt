package de.shyim.shopware6.action.generator.vue

import com.intellij.icons.AllIcons
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates

class NewModuleAction: DumbAwareAction("Create a module", "Create a new Shopware Admin Module", AllIcons.FileTypes.JavaScript) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val ui = NewModuleDialogWrapper()
        val config = ui.showAndGetConfig() ?: return

        // Create folder

        val folder = ActionUtil.getViewDirectory(e.dataContext) ?: return

        if (folder.findSubdirectory(config.name) != null) {
            Messages.showInfoMessage("Module already exists", "Error")
            return
        }

        val moduleFolder = ActionUtil.createDirectory(folder, config.name) ?: return

        val project = e.project!!

        // Create module root file

        val content = ShopwareTemplates.renderTemplate(
            project,
            ShopwareTemplates.SHOPWARE_ADMIN_VUE_MODULE,
            config.toMap()
        )

        ActionUtil.createFile(
            project,
            JavaScriptFileType.INSTANCE,
            "index.js",
            content,
            moduleFolder
        )

        val componentFolder = ActionUtil.createDirectory(moduleFolder, "component") ?: return

        val componentAction = NewComponentAction()

        componentAction.createComponent(
            project, componentFolder, NewComponentConfig(
                config.name + "-index",
                generateCss = true,
                generateTwig = true
            )
        )

        // Create snippet files

        val snippetFolder = ActionUtil.createDirectory(moduleFolder, "snippet") ?: return

        createSnippet(project, snippetFolder, config.name, "de-DE")
        createSnippet(project, snippetFolder, config.name, "en-GB")

        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext) ?: return
        val psiFile = moduleFolder.findFile("index.js")
        if (psiFile != null) {
            view.selectElement(psiFile)
        }
    }

    private fun createSnippet(project: Project, directory: PsiDirectory, name: String, language: String) {
        val content = ShopwareTemplates.renderTemplate(
            project,
            ShopwareTemplates.SHOPWARE_ADMIN_VUE_MODULE_SNIPPET_JSON,
            mapOf(
                "NAME" to name
            )
        )

        val factory = PsiFileFactory.getInstance(project)
        val file = factory.createFileFromText(language + ".json", JavaScriptFileType.INSTANCE, content)

        ApplicationManager.getApplication().runWriteAction {
            CodeStyleManager.getInstance(project).reformat(file)
            directory.add(file)
        }
    }
}