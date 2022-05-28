package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FeatureFlagIndex
import de.shyim.shopware6.index.dict.FeatureFlag
import icons.ShopwareToolBoxIcons

object FeatureFlagUtil {
    private fun getAllFlags(project: Project): MutableList<FeatureFlag> {
        val flags: MutableList<FeatureFlag> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(FeatureFlagIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(FeatureFlagIndex.key, key, GlobalSearchScope.allScope(project))

            flags.addAll(values)
        }

        return flags
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllFlags(project).forEach {
            list.add(
                LookupElementBuilder.create(it.name).withTypeText(it.description)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }
}