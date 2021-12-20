package de.shyim.shopware6.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiDirectory

object PsiFolderUtil {
    fun createFolderRecursive(startDir: PsiDirectory, folders: String): PsiDirectory {
        var folder = startDir
        val paths = folders.split("/")

        ApplicationManager.getApplication().runWriteAction {
            paths.forEach {
                val subFolder = folder.findSubdirectory(it)

                folder = if (subFolder == null) {
                    folder.createSubdirectory(it)
                } else {
                    subFolder
                }
            }
        }

        return folder
    }
}

