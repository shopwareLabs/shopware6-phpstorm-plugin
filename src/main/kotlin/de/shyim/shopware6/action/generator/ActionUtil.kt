package de.shyim.shopware6.action.generator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import java.io.IOException


class ActionUtil {
    companion object {
        fun buildFile(event: AnActionEvent, project: Project, templatePath: String, fileName: String?, fileType: LanguageFileType) {
            val dataContext = event.dataContext
            val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return
            val directories = view.directories
            if (directories.isEmpty()) {
                return
            }
            val initialBaseDir = directories[0] ?: return
            if (initialBaseDir.findFile(fileName!!) != null) {
                Messages.showInfoMessage("File exists", "Error")
                return
            }
            val content: String
            content = try {
                StreamUtil.readText(ActionUtil::class.java.getResourceAsStream(templatePath), "UTF-8")
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
            val factory = PsiFileFactory.getInstance(project)
            val file = factory.createFileFromText(fileName, fileType!!, content)
            ApplicationManager.getApplication().runWriteAction {
                CodeStyleManager.getInstance(project).reformat(file)
                initialBaseDir.add(file)
            }
            val psiFile = initialBaseDir.findFile(fileName)
            if (psiFile != null) {
                view.selectElement(psiFile)
            }
        }
    }
}