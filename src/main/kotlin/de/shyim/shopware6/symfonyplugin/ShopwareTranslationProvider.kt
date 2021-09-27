package de.shyim.shopware6.symfonyplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import de.shyim.shopware6.util.FrontendSnippetUtil
import de.shyim.shopware6.util.SnippetUtil
import fr.adrienbrault.idea.symfony2plugin.extension.TranslatorProvider
import fr.adrienbrault.idea.symfony2plugin.extension.TranslatorProviderDict
import java.util.*

class ShopwareTranslationProvider : TranslatorProvider {
    override fun hasTranslationKey(project: Project, transKey: String, p2: String): Boolean {
        FrontendSnippetUtil.getAllSnippets(project).forEach { it ->
            if (it.snippets.filterKeys { it == transKey }.isNotEmpty()) {
                return true
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

        FrontendSnippetUtil.getAllSnippets(project).forEach {
            if (it.snippets.filterKeys { it == transKey }.isNotEmpty()) {
                val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                if (file != null) {
                    val psi = PsiManager.getInstance(project).findFile(file)

                    if (psi != null) {
                        psi.containingDirectory?.files?.forEach { psiFile ->
                            if (psiFile.name.endsWith(".json")) {
                                psiElements.add(SnippetUtil.getTargets(psiFile, transKey))
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

        FrontendSnippetUtil.getAllSnippets(project).forEach {
            val file = LocalFileSystem.getInstance().findFileByPath(it.file)

            if (file != null) {
                files.add(file)
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