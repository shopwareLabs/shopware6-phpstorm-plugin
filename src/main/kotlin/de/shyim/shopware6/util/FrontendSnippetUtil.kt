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
            val values = FileBasedIndex.getInstance()
                .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippets.addAll(values)
        }

        return snippets
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()
        val usedKeys: MutableList<String> = ArrayList()

        getAllSnippets(project).forEach { file ->
            file.snippets.forEach { snippet ->
                if (usedKeys.contains(snippet.key)) {
                    return@forEach
                }

                list.add(
                    LookupElementBuilder.create(snippet.key).withTypeText(snippet.value)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )

                usedKeys.add(snippet.key)
            }
        }

        return list
    }
}