package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FrontendSnippetIndex
import de.shyim.shopware6.util.TwigPattern


class FrontendSnippetGoToHandler: GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        sourceElement: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (editor === null || editor.project === null || sourceElement == null) {
            return null
        }

        val project = editor.project!!

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (TwigPattern.getTranslationKeyPattern("trans", "transchoice").accepts(sourceElement)) {
            val snippetInfo = FileBasedIndex.getInstance().getValues(FrontendSnippetIndex.key, sourceElement.text, GlobalSearchScope.allScope(project))

            if (snippetInfo.size > 0) {
                val file = LocalFileSystem.getInstance().findFileByPath(snippetInfo[0].last())
                if (file != null) {
                    val psi = PsiManager.getInstance(project).findFile(file)

                    if (psi != null) {
                        psiElements.add(psi!!)
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}