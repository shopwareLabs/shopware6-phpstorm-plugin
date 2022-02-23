package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ShopwareAppIndex
import de.shyim.shopware6.index.dict.ShopwareApp

object ShopwareAppUtil {
    fun getAllApps(project: Project): MutableList<ShopwareApp> {
        val apps: MutableList<ShopwareApp> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(ShopwareAppIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(ShopwareAppIndex.key, key, GlobalSearchScope.allScope(project))

            apps.addAll(vals)
        }

        return apps
    }
}