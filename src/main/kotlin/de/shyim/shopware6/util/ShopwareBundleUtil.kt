package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ShopwareBundleIndex
import de.shyim.shopware6.index.dict.ShopwareBundle

object ShopwareBundleUtil {
    private val NON_VIEW_SHOPWARE_BUNDLES =
        arrayOf("Administration", "DevOps", "Checkout", "Profiling", "Elasticsearch", "Content", "System", "Framework")

    fun getAllBundles(project: Project): MutableList<ShopwareBundle> {
        val bundles: MutableList<ShopwareBundle> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(ShopwareBundleIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(ShopwareBundleIndex.key, key, GlobalSearchScope.allScope(project))

            bundles.addAll(vals)
        }

        return bundles
    }

    fun getAllBundlesRelatedToViews(project: Project): List<ShopwareBundle> {
        return getAllBundles(project).filter {
            !NON_VIEW_SHOPWARE_BUNDLES.contains(it.name)
        }
    }
}