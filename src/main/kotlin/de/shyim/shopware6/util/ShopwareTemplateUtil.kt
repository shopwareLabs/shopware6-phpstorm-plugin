package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.jetbrains.twig.TwigFileType
import icons.ShopwareToolBoxIcons

object ShopwareTemplateUtil {
    fun findTemplateInBundle(project: Project, bundleName: String, templatePath: String): VirtualFile? {
        val suffix = "Resources/views/$templatePath"

        return filterByBundle(getTemplatesByViewPath(project, templatePath), suffix, bundleName)
            .sortedWith(templateOrder())
            .firstOrNull()
    }

    fun resolveTemplateReference(project: Project, reference: String): List<VirtualFile> {
        val bundleName: String?
        val templatePath: String

        if (reference.startsWith("@")) {
            bundleName = reference.substring(1).substringBefore("/")
            templatePath = reference.substringAfter("/", "")
        } else {
            bundleName = null
            templatePath = reference
        }

        if (templatePath.isEmpty()) {
            return emptyList()
        }

        val candidates = getTemplatesByViewPath(project, templatePath)

        if (bundleName == null) {
            return candidates.sortedWith(templateOrder())
        }

        // the referenced bundle first, then every other template with the same view path, as
        // they are all part of the inheritance chain at runtime
        val bundleMatches = filterByBundle(candidates, "Resources/views/$templatePath", bundleName)

        return bundleMatches.sortedWith(templateOrder()) +
                (candidates - bundleMatches.toSet()).sortedWith(templateOrder())
    }

    fun getTemplateLookupElements(project: Project): List<LookupElement> {
        val elements = HashMap<String, LookupElement>()

        FileTypeIndex.getFiles(TwigFileType.INSTANCE, GlobalSearchScope.allScope(project)).forEach { file ->
            if (!file.path.contains("Resources/views/")) {
                return@forEach
            }

            val bundleName = getBundleNameForPath(file.path) ?: return@forEach
            val reference = "@$bundleName/${TwigUtil.getRelativePath(file.path)}"

            elements.putIfAbsent(
                reference,
                LookupElementBuilder.create(reference).withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return elements.values.toList()
    }

    fun getBundleNameForPath(path: String): String? {
        if (TwigUtil.isShopwareCoreTemplate(path)) {
            return "Storefront"
        }

        if (path.contains("custom/plugins/")) {
            return path.substringAfterLast("custom/plugins/").substringBefore("/")
        }

        if (path.contains("custom/apps/")) {
            return path.substringAfterLast("custom/apps/").substringBefore("/")
        }

        if (path.contains("vendor/")) {
            val parts = path.substringAfterLast("vendor/").split("/")

            if (parts.size < 3) {
                return null
            }

            // shopware/core -> Core, acme/example-plugin -> AcmeExamplePlugin
            return if (parts[0] == "shopware") {
                camelize(parts[1])
            } else {
                camelize(parts[0]) + camelize(parts[1])
            }
        }

        val root = path.substringBefore("Resources/views/").split("/").filter { it.isNotEmpty() }
        val last = root.lastOrNull() ?: return null

        return if (last == "src") root.getOrNull(root.size - 2) else last
    }

    private fun getTemplatesByViewPath(project: Project, templatePath: String): List<VirtualFile> {
        val suffix = "Resources/views/$templatePath"

        return FilenameIndex.getVirtualFilesByName(
            templatePath.substringAfterLast('/'),
            GlobalSearchScope.allScope(project)
        ).filter { it.path.endsWith(suffix) }
    }

    private fun filterByBundle(
        candidates: List<VirtualFile>,
        suffix: String,
        bundleName: String
    ): List<VirtualFile> {
        val normalizedBundle = normalize(bundleName)

        // prefer a path segment exactly matching the bundle name, fall back to a substring
        // match to also cover vendor packages (@AcmeFoo -> vendor/acme/foo)
        return candidates.filter { candidate ->
            candidate.path.removeSuffix(suffix).split('/').any { normalize(it) == normalizedBundle }
        }.ifEmpty {
            candidates.filter { candidate ->
                normalize(candidate.path.removeSuffix(suffix)).contains(normalizedBundle)
            }
        }
    }

    private fun templateOrder(): Comparator<VirtualFile> {
        return compareByDescending<VirtualFile> { TwigUtil.isShopwareCoreTemplate(it.path) }.thenBy { it.path }
    }

    private fun normalize(value: String): String {
        return value.replace("-", "").replace("_", "").replace("/", "").lowercase()
    }

    private fun camelize(value: String): String {
        return value.split("-", "_").joinToString("") { part ->
            part.replaceFirstChar { it.uppercase() }
        }
    }
}
