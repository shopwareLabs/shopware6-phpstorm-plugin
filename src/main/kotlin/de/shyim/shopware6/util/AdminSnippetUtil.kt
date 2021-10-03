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
    fun getAllSnippets(project: Project): MutableList<SnippetFile> {
        val snippets: MutableList<SnippetFile> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(AdminSnippetIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(AdminSnippetIndex.key, key, GlobalSearchScope.allScope(project))

            snippets.addAll(vals)
        }

        return snippets
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()
        val usedKeys: MutableList<String> = ArrayList()

        getAllSnippets(project).forEach {
            it.snippets.forEach {
                if (usedKeys.contains(it.key)) {
                    return@forEach
                }

                list.add(
                    LookupElementBuilder.create(it.key).withTypeText(it.value)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )

                usedKeys.add(it.key)
            }
        }

        return list
    }

    fun hasSnippet(key: String, project: Project): Boolean {
        getAllSnippets(project).forEach { snippetFile ->
            snippetFile.snippets.forEach {
                if (it.key == key) {
                    return true
                }
            }
        }

        return false
    }
}