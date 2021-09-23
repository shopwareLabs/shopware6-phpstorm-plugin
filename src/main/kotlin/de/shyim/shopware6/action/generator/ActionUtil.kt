package de.shyim.shopware6.action.generator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory


class ActionUtil {
    companion object {
        fun getViewDirectory(dataContext: DataContext): PsiDirectory? {
            val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return null
            val directories = view.directories
            if (directories.isEmpty()) {
                return null
            }
            return directories[0] ?: return null
        }

        fun buildFile(
            event: AnActionEvent,
            project: Project,
            content: String,
            fileName: String?,
            fileType: LanguageFileType
        ) {
            val dataContext = event.dataContext
            val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return
            val initialBaseDir = this.getViewDirectory(dataContext) ?: return

            if (initialBaseDir.findFile(fileName!!) != null) {
                Messages.showInfoMessage("File exists", "Error")
                return
            }

            val factory = PsiFileFactory.getInstance(project)
            val file = factory.createFileFromText(fileName, fileType, content)
            ApplicationManager.getApplication().runWriteAction {
                initialBaseDir.add(file)
            }
            val psiFile = initialBaseDir.findFile(fileName)
            if (psiFile != null) {
                view.selectElement(psiFile)
            }
        }
    }
}