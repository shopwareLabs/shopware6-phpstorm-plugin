package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
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

        if (TwigPattern.getTranslationKeyPattern("trans").accepts(sourceElement)) {
            for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
                val snippetInfo = FileBasedIndex.getInstance()
                    .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

                snippetInfo.forEach {
                    if (it.snippets.filterKeys { it == sourceElement.text }.isNotEmpty()) {
                        val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                        if (file != null) {
                            val psi = PsiManager.getInstance(project).findFile(file)

                            if (psi != null) {
                                val snippetParts = sourceElement.text.split(".") as MutableList
                                var foundPsi = false

                                psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                    override fun visitElement(element: PsiElement) {
                                        if (snippetParts.isEmpty()) {
                                            return
                                        }

                                        if (element is JsonProperty) {
                                            if (element.firstChild.firstChild.text == "\"" + snippetParts[0] + "\"") {
                                                snippetParts.removeAt(0)

                                                if (snippetParts.isEmpty()) {
                                                    psiElements.add(element)
                                                    foundPsi = true
                                                    return
                                                }

                                                super.visitElement(element)
                                            }
                                        }

                                        super.visitElement(element)
                                    }
                                })

                                if (!foundPsi) {
                                    psiElements.add(psi)
                                }
                            }
                        }
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}