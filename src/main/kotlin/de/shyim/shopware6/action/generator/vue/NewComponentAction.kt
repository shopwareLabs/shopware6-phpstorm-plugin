package de.shyim.shopware6.action.generator.vue

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates

class NewComponentAction: DumbAwareAction("Create a component", "Create a new Vue component", AllIcons.FileTypes.JavaScript) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val dialog = NewComponentDialogWrapper()
        val config = dialog.showAndGetName() ?: return

        // Create folder

        val folder = ActionUtil.getViewDirectory(e.dataContext) ?: return

        if (folder.findSubdirectory(config.name) != null) {
            Messages.showInfoMessage("Component already exists", "Error")
            return
        }

        var componentFolder: PsiDirectory? = null
        ApplicationManager.getApplication().runWriteAction {
            componentFolder = folder.createSubdirectory(config.name)
        }

        if (componentFolder == null) {
            return
        }

        // Create index.js
        val content = ShopwareTemplates.applyShopwareAdminVueComponent(
            e.project!!,
            ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT,
            config
        )

        val factory = PsiFileFactory.getInstance(e.project)
        val file = factory.createFileFromText("index.js", JavaScriptFileType.INSTANCE, content)

        ApplicationManager.getApplication().runWriteAction {
            CodeStyleManager.getInstance(e.project!!).reformat(file)
            componentFolder!!.add(file)
        }

        if (config.generateTwig) {
            // Create html file

            val htmlFile = factory.createFileFromText(
                "${config.name}.html.twig",
                HtmlFileType.INSTANCE,
                ShopwareTemplates.applyShopwareAdminVueComponent(
                    e.project!!,
                    ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT_TWIG,
                    config
                )
            )

            ApplicationManager.getApplication().runWriteAction {
                CodeStyleManager.getInstance(e.project!!).reformat(htmlFile)
                componentFolder!!.add(htmlFile)
            }
        }


        if (config.generateCss) {
            // Create css file

            val cssFile = factory.createFileFromText(
                "${config.name}.scss",
                HtmlFileType.INSTANCE,
                ShopwareTemplates.applyShopwareAdminVueComponent(
                    e.project!!,
                    ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT_SCSS,
                    config
                )
            )

            ApplicationManager.getApplication().runWriteAction {
                CodeStyleManager.getInstance(e.project!!).reformat(cssFile)
                componentFolder!!.add(cssFile)
            }
        }

        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext) ?: return
        val psiFile = componentFolder!!.findFile("index.js")
        if (psiFile != null) {
            view.selectElement(psiFile)
        }
    }
}