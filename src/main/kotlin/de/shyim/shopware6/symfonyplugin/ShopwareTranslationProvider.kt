package de.shyim.shopware6.symfonyplugin

import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FrontendSnippetIndex
import fr.adrienbrault.idea.symfony2plugin.extension.TranslatorProvider
import fr.adrienbrault.idea.symfony2plugin.extension.TranslatorProviderDict
import java.util.*

class ShopwareTranslationProvider : TranslatorProvider {
    override fun hasTranslationKey(project: Project, transKey: String, p2: String): Boolean {
        for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
            val snippetInfo = FileBasedIndex.getInstance()
                .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippetInfo.forEach {
                if (it.snippets.filterKeys { it == transKey }.isNotEmpty()) {
                    return true
                }
            }
        }

        return false
    }

    override fun hasDomain(p0: Project, domain: String): Boolean {
        return domain == "messages"
    }

    override fun getTranslationDomains(p0: Project): MutableCollection<TranslatorProviderDict.TranslationDomain> {
        return Collections.emptyList()
    }

    override fun getTranslationTargets(project: Project, transKey: String, p2: String): MutableCollection<PsiElement> {
        val psiElements: MutableList<PsiElement> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
            val snippetInfo = FileBasedIndex.getInstance()
                .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippetInfo.forEach {
                if (it.snippets.filterKeys { it == transKey }.isNotEmpty()) {
                    val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                    if (file != null) {
                        val psi = PsiManager.getInstance(project).findFile(file)

                        if (psi != null) {
                            val snippetParts = transKey.split(".") as MutableList
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

        return psiElements
    }

    override fun getDomainPsiFiles(project: Project?, domain: String): MutableCollection<VirtualFile> {
        val files: MutableList<VirtualFile> = ArrayList()

        if (project == null || domain != "messages") {
            return files
        }

        for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
            val snippetInfo = FileBasedIndex.getInstance()
                .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippetInfo.forEach {
                val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                if (file != null) {
                    files.add(file)
                }
            }
        }

        return files
    }

    override fun getTranslationsForDomain(
        project: Project,
        domain: String
    ): MutableCollection<TranslatorProviderDict.TranslationKey> {
        return Collections.emptyList()
    }
}