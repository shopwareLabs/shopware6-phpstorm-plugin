package de.shyim.shopware6.action.generator.vue

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates

class NewComponentAction :
    DumbAwareAction("Create a Component", "Create a new Vue component", AllIcons.FileTypes.JavaScript) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val dialog = NewComponentDialogWrapper()
        val config = dialog.showAndGetName() ?: return

        // Create folder

        val folder = ActionUtil.getViewDirectory(e.dataContext) ?: return

        val componentFolder = createComponent(e.project!!, folder, config)

        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext) ?: return
        val psiFile = componentFolder!!.findFile("index.js")
        if (psiFile != null) {
            view.selectElement(psiFile)
        }
    }

    fun createComponent(project: Project, folder: PsiDirectory, config: NewComponentConfig): PsiDirectory? {
        if (folder.findSubdirectory(config.name) != null) {
            Messages.showInfoMessage("Component already exists", "Error")
            return null
        }

        var componentFolder: PsiDirectory? = null
        ApplicationManager.getApplication().runWriteAction {
            componentFolder = folder.createSubdirectory(config.name)
        }

        if (componentFolder == null) {
            return null
        }

        // Create index.js
        val content = ShopwareTemplates.renderTemplate(
            project,
            ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT,
            config.toMap()
        )

        val factory = PsiFileFactory.getInstance(project)
        val file = factory.createFileFromText("index.js", JavaScriptFileType.INSTANCE, content)

        ApplicationManager.getApplication().runWriteAction {
            componentFolder!!.add(file)
        }

        if (config.generateTwig) {
            // Create html file

            val htmlFile = factory.createFileFromText(
                "${config.name}.html.twig",
                HtmlFileType.INSTANCE,
                ShopwareTemplates.renderTemplate(
                    project,
                    ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT_TWIG,
                    config.toMap()
                )
            )

            ApplicationManager.getApplication().runWriteAction {
                componentFolder!!.add(htmlFile)
            }
        }


        if (config.generateCss) {
            // Create css file

            val cssFile = factory.createFileFromText(
                "${config.name}.scss",
                HtmlFileType.INSTANCE,
                ShopwareTemplates.renderTemplate(
                    project,
                    ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT_SCSS,
                    config.toMap()
                )
            )

            ApplicationManager.getApplication().runWriteAction {
                componentFolder!!.add(cssFile)
            }
        }

        return componentFolder
    }
}