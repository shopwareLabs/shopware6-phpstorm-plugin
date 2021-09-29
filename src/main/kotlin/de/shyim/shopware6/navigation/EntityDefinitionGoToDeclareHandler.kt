package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import de.shyim.shopware6.util.EntityDefinitionUtil
import de.shyim.shopware6.util.JavaScriptPattern

class EntityDefinitionGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (editor === null || editor.project === null || element == null) {
            return null
        }

        val project = editor.project!!

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (JavaScriptPattern.getRepositoryFactoryCreatePattern().accepts(element)) {
            val text = element.text.replace("\"", "").replace("'", "")

            EntityDefinitionUtil.getAllDefinitions(project).forEach {
                if (it.name == text) {
                    val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                    if (file != null) {
                        val psi = PsiManager.getInstance(project).findFile(file)
                        if (psi != null) {
                            psiElements.add(psi)
                        }
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}