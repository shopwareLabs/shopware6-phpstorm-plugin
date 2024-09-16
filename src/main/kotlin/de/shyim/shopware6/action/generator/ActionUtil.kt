package de.shyim.shopware6.action.generator

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager


class ActionUtil {
    companion object {
        fun getViewDirectory(dataContext: DataContext): PsiDirectory? {
            val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return null
            val directories = view.directories
            if (directories.isEmpty()) {
                return null
            }
            return directories[0]
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

            createFile(
                project,
                fileType,
                fileName,
                content,
                initialBaseDir
            )

            val psiFile = initialBaseDir.findFile(fileName)
            if (psiFile != null) {
                view.selectElement(psiFile)
            }
        }

        fun createFile(
            project: Project,
            type: FileType,
            name: String,
            content: String,
            directory: PsiDirectory
        ): PsiFile? {
            if (directory.findFile(name) != null) {
                Messages.showInfoMessage("File $name already exists", "Error")
                return null
            }

            val factory = PsiFileFactory.getInstance(project)
            val file = factory.createFileFromText(name, type, content)

            ApplicationManager.getApplication().runWriteAction {
                CodeStyleManager.getInstance(project).reformat(file)
                directory.add(file)
            }

            return directory.findFile(name)
        }

        fun createDirectory(dir: PsiDirectory, name: String): PsiDirectory? {
            if (dir.findSubdirectory(name) != null) {
                Messages.showInfoMessage("Folder already exists", "Error")
                return null
            }

            var srcFolder: PsiDirectory? = null
            ApplicationManager.getApplication().runWriteAction {
                srcFolder = dir.createSubdirectory(name)
            }

            return srcFolder
        }
    }
}