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

                if (subFolder == null) {
                    folder = folder.createSubdirectory(it)
                } else {
                    folder = subFolder
                }
            }
        }

        return folder
    }
}

