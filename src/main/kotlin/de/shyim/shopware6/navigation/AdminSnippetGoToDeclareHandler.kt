package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminSnippetIndex
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.SnippetUtil
import de.shyim.shopware6.util.TwigPattern

class AdminSnippetGoToDeclareHandler : GotoDeclarationHandler {
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

        if (JavaScriptPattern.getTcPattern().accepts(element) || TwigPattern.getTcPattern().accepts(element)) {
            val text = element.text.replace("\"", "").replace("'", "")

            val keys = FileBasedIndex.getInstance().getAllKeys(AdminSnippetIndex.key, project)

            keys.forEach {
                val vals = FileBasedIndex.getInstance()
                    .getValues(AdminSnippetIndex.key, it, GlobalSearchScope.allScope(project))

                vals.forEach {
                    if (it.snippets.containsKey(text)) {
                        val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                        if (file != null) {
                            val psi = PsiManager.getInstance(project).findFile(file)

                            if (psi != null) {
                                psiElements.add(SnippetUtil.getTargets(psi, text))
                            }
                        }
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}