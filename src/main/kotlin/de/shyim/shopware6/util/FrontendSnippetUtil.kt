package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FrontendSnippetIndex
import de.shyim.shopware6.index.dict.SnippetFile
import icons.ShopwareToolBoxIcons

object FrontendSnippetUtil {
    fun getAllSnippets(project: Project): MutableList<SnippetFile> {
        val snippets: MutableList<SnippetFile> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippets.addAll(vals)
        }

        return snippets
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllSnippets(project).forEach {
            it.snippets.forEach {
                list.add(
                    LookupElementBuilder.create(it.key).withTypeText(it.value)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )
            }
        }

        return list
    }
}