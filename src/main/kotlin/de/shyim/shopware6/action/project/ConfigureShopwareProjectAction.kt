package de.shyim.shopware6.action.project

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.LocalFileSystem
import de.shyim.shopware6.util.ShopwareBundleUtil
import icons.ShopwareToolBoxIcons
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.codehaus.jettison.json.JSONObject
import java.io.File

class ConfigureShopwareProjectAction : DumbAwareAction(
    "Configure Shopware Project",
    "Configure Shopware Project with IDE",
    ShopwareToolBoxIcons.SHOPWARE
) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val model = ModuleRootManager.getInstance(ModuleManager.getInstance(e.project!!).modules[0]).modifiableModel
        val basePath = e.project!!.basePath + "/src"

        val entry = model.contentEntries.firstOrNull()
        if (entry != null) {
            addShopwareExcludes(entry)
            adjustTestNamespaces(entry, e.project!!)
        }

        ShopwareBundleUtil.getAllBundles(e.project!!).forEach { shopwareBundle ->
            if (shopwareBundle.path.startsWith(basePath)) {
                return@forEach
            }

            var foundContentEntry: ContentEntry? = null

            model.contentEntries.forEach { contentEntry ->
                if (contentEntry.url == shopwareBundle.composerFolder) {
                    foundContentEntry = contentEntry
                }
            }

            if (foundContentEntry == null) {
                foundContentEntry =
                    model.addContentEntry(LocalFileSystem.getInstance().findFileByPath(shopwareBundle.composerFolder)!!)
            }

            val composerJsonBody =
                FileUtils.readFileToString(File("${shopwareBundle.composerFolder}/composer.json"), "UTF-8")
            val jsonObject = JSONObject(composerJsonBody)

            addSourceFolders("autoload", jsonObject, foundContentEntry!!, shopwareBundle.composerFolder)
            addSourceFolders("autoload-dev", jsonObject, foundContentEntry!!, shopwareBundle.composerFolder)
        }

        CommandProcessor.getInstance().executeCommand(
            e.project!!, {
                ApplicationManager.getApplication().runWriteAction {
                    model.commit()
                }
            },
            "Updating Project Settings", null
        )
    }

    private fun addShopwareExcludes(model: ContentEntry) {
        val excludes = arrayOf(
            "var/cache",
            "var/log",
            ".direnv",
            ".devenv",
            "files",
            "public/bundles",
            "public/media",
            "public/recovery",
            "public/theme",
            "public/thumbnail",
            "vendor-bin",

            // Platform specific
            "src/Administration/Resources/public",
            "src/Administration/Resources/app/administration/.jestcache",
            "src/Administration/Resources/app/administration/build",
            "src/Storefront/Resources/public",
            "src/Storefront/Resources/app/storefront/dist",
            "src/Storefront/Resources/app/storefront/vendor/bootstrap/dist",
            "src/Storefront/Resources/app/storefront/vendor/flatpickr/dist",
            "src/Storefront/Resources/app/storefront/vendor/tiny-slider/dist",

            // Template specific
            "vendor/shopware/administration/Resources/public",
            "vendor/shopware/administration/app/administration/.jestcache",
            "vendor/shopware/administration/app/administration/build",
            "vendor/shopware/storefront/Resources/public",
            "vendor/shopware/storefront/Resources/app/storefront/dist",
            "vendor/shopware/storefront/Resources/app/storefront/vendor/bootstrap/dist",
            "vendor/shopware/storefront/Resources/app/storefront/vendor/flatpickr/dist",
            "vendor/shopware/storefront/Resources/app/storefront/vendor/tiny-slider/dist",
        )

        excludes.forEach { exclude ->
            if (LocalFileSystem.getInstance().findFileByPath("${model.file!!.path}/${exclude}") != null) {
                model.addExcludeFolder("file://${model.file!!.path}/${exclude}")
            }
        }
    }

    private fun addSourceFolders(
        type: String,
        jsonObject: JSONObject,
        foundContentEntry: ContentEntry,
        basePath: String
    ) {
        if (!jsonObject.has(type)) {
            return
        }

        if (!jsonObject.getJSONObject(type).has("psr-4")) {
            return
        }

        val namespaces = jsonObject.getJSONObject(type).getJSONObject("psr-4")

        val it = namespaces.keys()
        while (it.hasNext()) {
            val key = it.next().toString()

            if (!namespaces.has(key)) {
                continue
            }

            val bundleFolder =
                LocalFileSystem.getInstance().findFileByPath("${basePath}/${namespaces.getString(key)}") ?: continue

            var alreadyRegistered = false

            foundContentEntry.sourceFolders.forEach { sourceFolder ->
                if (StringUtils.removeEnd(
                        sourceFolder?.file.toString(),
                        "/"
                    ) == StringUtils.removeEnd(bundleFolder.toString(), "/")
                ) {
                    sourceFolder.packagePrefix = key
                    alreadyRegistered = true
                }
            }

            if (alreadyRegistered) {
                continue
            }

            foundContentEntry.addSourceFolder(bundleFolder, type == "autoload-dev", key)
        }
    }

    private fun adjustTestNamespaces(entry: ContentEntry, project: Project) {
        val composerJsonPath = "${project.basePath}/composer.json"

        if (!File(composerJsonPath).exists()) {
            return
        }

        val composerJsonBody = FileUtils.readFileToString(File(composerJsonPath), "UTF-8")
        val jsonObject = JSONObject(composerJsonBody)

        if (jsonObject.getString("name") != "shopware/platform") {
            return
        }

        val composerInvalidAutoDetections = arrayOf(
            "src/Administration/Test",
            "src/Core/Checkout/Test",
            "src/Core/Content/Test",
            "src/Core/DevOps/Test",
            "src/Core/Framework/Test",
            "src/Core/Maintenance/Test",
            "src/Core/System/Test",
            "src/Core/Promotion/Test",
            "src/Core/Migration/Test",
            "src/Core/Test",
            "src/Storefront/Test",
            "src/Elasticsearch/Test",
            "src/WebInstaller/tests",
            "tests",
            "tests/performance/bench",
            "spec",
            "vendor-bin/roave-backward-compatibility-check/tests",
        )

        entry.sourceFolders.forEach {
            val relative = it.url.replace("file://${project.basePath}/", "")

            if (!it.isTestSource) {
                if (it.url.contains("Framework/Test/") || relative.startsWith("tests/") || relative.startsWith("vendor-bin/") || relative.startsWith("src/Administration/Resources")) {
                    entry.removeSourceFolder(it)
                }
                return@forEach
            }


            if (composerInvalidAutoDetections.contains(relative)) {
                entry.removeSourceFolder(it)
            }
        }
    }
}