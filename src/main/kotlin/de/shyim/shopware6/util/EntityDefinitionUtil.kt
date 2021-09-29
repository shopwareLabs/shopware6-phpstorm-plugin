package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.EntityDefinitionIndex
import de.shyim.shopware6.index.dict.EntityDefinition
import icons.ShopwareToolBoxIcons

object EntityDefinitionUtil {
    fun getAllDefinitions(project: Project): MutableList<EntityDefinition> {
        val definitions: MutableList<EntityDefinition> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(EntityDefinitionIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(EntityDefinitionIndex.key, key, GlobalSearchScope.allScope(project))

            definitions.addAll(vals)
        }

        return definitions
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllDefinitions(project).forEach {
            list.add(
                LookupElementBuilder.create(it.name)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }
}