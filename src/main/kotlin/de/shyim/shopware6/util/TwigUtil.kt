package de.shyim.shopware6.util

import com.intellij.openapi.project.Project

object TwigUtil {
    fun getTemplatePathByFilePath(filePath: String, project: Project): String? {
        var path: String? = null
        ShopwareBundleUtil.getAllBundles(project).forEach { bundle ->
            if (bundle.viewPath != null && filePath.startsWith(bundle.viewPath)) {
                path = filePath.replace(bundle.viewPath, "")
            }
        }

        return path
    }

    fun getBundleByFilePath(filePath: String, project: Project): String? {
        var name: String? = null
        ShopwareBundleUtil.getAllBundles(project).forEach { bundle ->
            if (bundle.viewPath != null && filePath.startsWith(bundle.viewPath)) {
                name = bundle.name
            }
        }

        return name
    }
}