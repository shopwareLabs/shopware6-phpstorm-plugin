package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminSnippetIndex
import de.shyim.shopware6.index.dict.SnippetFile
import icons.ShopwareToolBoxIcons

object AdminSnippetUtil {
    private fun getAllSnippets(project: Project): MutableList<SnippetFile> {
        val snippets: MutableList<SnippetFile> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(AdminSnippetIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(AdminSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippets.addAll(values)
        }

        return snippets
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()
        val usedKeys: MutableList<String> = ArrayList()

        getAllSnippets(project).forEach { file ->
            file.snippets.forEach snippetLoop@{ snippets ->
                if (usedKeys.contains(snippets.key)) {
                    return@snippetLoop
                }

                list.add(
                    LookupElementBuilder.create(snippets.key).withTypeText(snippets.value)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )

                usedKeys.add(snippets.key)
            }
        }

        return list
    }

    fun hasSnippet(key: String, project: Project): Boolean {
        return getAllSnippets(project).any { snippetFile ->
            snippetFile.snippets.containsKey(key)
        }
    }
}