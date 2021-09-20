package de.shyim.shopware6.action.generator

import com.intellij.icons.AllIcons
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.util.DirectoryChooser
import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import de.shyim.shopware6.action.generator.ui.NewComponentDialogWrapper
import java.io.IOException

class NewComponentAction: DumbAwareAction("Create a component", "Create a new Vue component", AllIcons.FileTypes.JavaScript) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val dialog = NewComponentDialogWrapper()
        val componentName = dialog.showAndGetName()

        if (componentName == "") {
            return
        }

        // Create folder

        val folder = ActionUtil.getViewDirectory(e.dataContext) ?: return

        if (folder.findSubdirectory(componentName) != null) {
            Messages.showInfoMessage("Component already exists", "Error")
            return
        }

        var componentFolder: PsiDirectory? = null
        ApplicationManager.getApplication().runWriteAction {
            componentFolder = folder.createSubdirectory(componentName)
        }

        if (componentFolder == null) {
            return
        }

        // Create index.js

        var content: String
        content = try {
            StreamUtil.readText(ActionUtil::class.java.getResourceAsStream("/fileTemplates/vue/component/index.js"), "UTF-8")
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        content = content.replace("#COMPONENT-NAME#", componentName)

        val factory = PsiFileFactory.getInstance(e.project)
        val file = factory.createFileFromText("index.js", JavaScriptFileType.INSTANCE, content)

        ApplicationManager.getApplication().runWriteAction {
            CodeStyleManager.getInstance(e.project!!).reformat(file)
            componentFolder!!.add(file)
        }

        // Create html file

        val htmlFile = factory.createFileFromText("$componentName.html.twig", HtmlFileType.INSTANCE, "")

        ApplicationManager.getApplication().runWriteAction {
            CodeStyleManager.getInstance(e.project!!).reformat(htmlFile)
            componentFolder!!.add(htmlFile)
        }

        // Create css file

        val cssFile = factory.createFileFromText("$componentName.scss", HtmlFileType.INSTANCE, "")

        ApplicationManager.getApplication().runWriteAction {
            CodeStyleManager.getInstance(e.project!!).reformat(cssFile)
            componentFolder!!.add(cssFile)
        }
    }
}