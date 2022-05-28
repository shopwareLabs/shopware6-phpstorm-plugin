package de.shyim.shopware6.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager

object PsiUtil {
    fun createFolderRecursive(startDir: PsiDirectory, folders: String): PsiDirectory {
        var folder = startDir
        val paths = folders.split("/")

        ApplicationManager.getApplication().runWriteAction {
            paths.forEach {
                folder = folder.findSubdirectory(it) ?: folder.createSubdirectory(it)
            }
        }

        return folder
    }

    fun addPsiFileToList(
        file: String,
        project: Project,
        psiElements: MutableList<PsiElement>
    ) {
        val file = LocalFileSystem.getInstance().findFileByPath(file)

        if (file != null) {
            val psi = PsiManager.getInstance(project).findFile(file)

            if (psi != null) {
                psiElements.add(psi)
            }
        }
    }
}

