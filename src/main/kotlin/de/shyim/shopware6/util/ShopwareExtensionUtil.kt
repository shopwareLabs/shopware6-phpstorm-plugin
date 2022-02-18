package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import de.shyim.shopware6.index.dict.ShopwareExtension

object ShopwareExtensionUtil {
    fun getAllExtensions(project: Project): MutableList<ShopwareExtension> {
        val extensions: MutableList<ShopwareExtension> = ArrayList()

        extensions.addAll(ShopwareBundleUtil.getAllBundlesRelatedToViews(project))
        extensions.addAll(ShopwareAppUtil.getAllApps(project))

        return extensions
    }
}