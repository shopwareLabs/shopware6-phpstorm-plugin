package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ShopwareAppIndex
import de.shyim.shopware6.index.dict.ShopwareApp
import java.nio.file.Paths
import kotlin.io.path.name

object ShopwareAppUtil {
    fun getAllApps(project: Project): MutableList<ShopwareApp> {
        val apps: MutableList<ShopwareApp> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(ShopwareAppIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(ShopwareAppIndex.key, key, GlobalSearchScope.allScope(project))

            apps.addAll(values)
        }

        return apps
    }

    fun getAppByFolderName(name: String, project: Project): ShopwareApp? {
        for (key in FileBasedIndex.getInstance().getAllKeys(ShopwareAppIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(ShopwareAppIndex.key, key, GlobalSearchScope.allScope(project))

            values.forEach {
                if (Paths.get(it.rootFolder).name == name) {
                    return it
                }
            }
        }

        return null
    }
}