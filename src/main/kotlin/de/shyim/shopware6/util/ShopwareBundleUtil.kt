package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ShopwareBundleIndex
import de.shyim.shopware6.index.dict.ShopwareBundle

object ShopwareBundleUtil {
    fun getAllBundles(project: Project): MutableList<ShopwareBundle> {
        val bundles: MutableList<ShopwareBundle> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(ShopwareBundleIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(ShopwareBundleIndex.key, key, GlobalSearchScope.allScope(project))

            bundles.addAll(vals)
        }

        return bundles
    }

    fun getAllBundlesWithViewDirectory(project: Project): List<ShopwareBundle> {
        return getAllBundles(project).filter { bundle ->
            return@filter bundle.viewPath != null
        }
    }
}