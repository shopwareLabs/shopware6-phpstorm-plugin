package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.completion.SnippetCompletionElement
import de.shyim.shopware6.index.AdminSnippetIndex
import de.shyim.shopware6.index.dict.SnippetFile
import icons.ShopwareToolBoxIcons

object AdminSnippetUtil {
    private fun getAllSnippetFiles(project: Project): MutableList<SnippetFile> {
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

        getAllSnippetValues(project).forEach {
            list.add(
                LookupElementBuilder.create(it.key).withTypeText(it.value)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }

    fun getAllSnippetValues(project: Project): MutableMap<String, String> {
        val keys: MutableMap<String, String> = mutableMapOf()

        getAllSnippetFiles(project).forEach { file ->
            file.snippets.forEach snippetLoop@{ snippets ->
                if (keys.contains(snippets.key)) {
                    return@snippetLoop
                }

                keys[snippets.key] = snippets.value
            }
        }

        return keys
    }

    fun hasSnippet(key: String, project: Project): Boolean {
        return getAllSnippetFiles(project).any { snippetFile ->
            snippetFile.snippets.containsKey(key)
        }
    }

    fun getAllEnglishKeys(project: Project): MutableList<SnippetCompletionElement> {
        val keys = mutableListOf<SnippetCompletionElement>()

        FrontendSnippetUtil.getAllSnippets(project).forEach { file ->
            if (!file.file.endsWith("en-GB.json")) {
                return@forEach
            }

            file.snippets.forEach snippetLoop@{ snippet ->
                keys.add(SnippetCompletionElement(snippet.key, snippet.value))
            }
        }

        return keys
    }
}