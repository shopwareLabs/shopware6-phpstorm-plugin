package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ShopwareTemplateExtendsIndex
import de.shyim.shopware6.index.ShopwareTemplateIndex
import icons.ShopwareToolBoxIcons

object ShopwareTemplateUtil {
    fun findTemplateInBundle(project: Project, bundleName: String, templatePath: String): VirtualFile? {
        return filterByBundle(getTemplatesByViewPath(project, templatePath), templatePath, bundleName)
            .sortedWith(templateOrder())
            .firstOrNull()
    }

    fun findTemplateByPath(project: Project, path: String): VirtualFile? {
        return getTemplatesByViewPath(project, TwigUtil.getRelativePath(path)).firstOrNull { it.path == path }
    }

    fun getExtendsTarget(project: Project, file: VirtualFile): String? {
        var target: String? = null

        FileBasedIndex.getInstance().processValues(
            ShopwareTemplateIndex.key,
            TwigUtil.getRelativePath(file.path),
            file,
            { _, value ->
                target = value
                true
            },
            GlobalSearchScope.allScope(project)
        )

        return target?.takeIf { it.isNotEmpty() }
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
        val bundleMatches = filterByBundle(candidates, templatePath, bundleName)

        return bundleMatches.sortedWith(templateOrder()) +
                (candidates - bundleMatches.toSet()).sortedWith(templateOrder())
    }

    /**
     * Every template extending the given one, nearest child first. Templates extending a child
     * are collected transitively.
     */
    fun getTemplatesExtendingTemplate(project: Project, file: VirtualFile): List<VirtualFile> {
        val children = LinkedHashSet<VirtualFile>()

        // several extensions typically override the same view path, so every one of them
        // references it and looks like a child of the others. The templates the file extends
        // itself are upstream, never children
        val visited = HashSet(getExtendsChainPaths(project, file))
        visited.add(file.path)

        var current = listOf(file)
        var depth = 0

        // a cycle is already broken by "visited", the depth limit only guards against
        // pathologically long chains
        while (current.isNotEmpty() && depth++ < 8) {
            val next = ArrayList<VirtualFile>()

            current.forEach { template ->
                getDirectChildren(project, template).forEach { child ->
                    if (visited.add(child.path)) {
                        children.add(child)
                        next.add(child)
                    }
                }
            }

            current = next
        }

        return children.toList()
    }

    /**
     * Paths of the templates the given one extends, nearest parent first
     */
    fun getExtendsChainPaths(project: Project, file: VirtualFile): List<String> {
        return followExtendsChain(project, file.path, getExtendsTarget(project, file))
    }

    /**
     * Walks up the sw_extends chain, starting at the given target reference, nearest parent
     * first. The targets of the parents come from the index, so no further files are loaded.
     */
    fun followExtendsChain(project: Project, startPath: String?, startTarget: String?): List<String> {
        val paths = ArrayList<String>()
        val visited = HashSet<String>()
        startPath?.let { visited.add(it) }

        var target = startTarget

        while (target != null && paths.size < 10) {
            val bundleName = target.substringBefore("/").removePrefix("@")
            val templatePath = target.substringAfter("/", "")

            if (templatePath.isEmpty()) {
                break
            }

            val parent = findTemplateInBundle(project, bundleName, templatePath) ?: break

            if (!visited.add(parent.path)) {
                break
            }

            paths.add(parent.path)
            target = getExtendsTarget(project, parent)
        }

        return paths
    }

    private fun getDirectChildren(project: Project, file: VirtualFile): List<VirtualFile> {
        return FileBasedIndex.getInstance().getContainingFiles(
            ShopwareTemplateExtendsIndex.key,
            TwigUtil.getRelativePath(file.path),
            GlobalSearchScope.allScope(project)
        ).filter { it.path != file.path }.sortedWith(templateOrder())
    }

    fun getTemplateLookupElements(project: Project): List<LookupElement> {
        // the template set only changes when files are created, moved or deleted
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            CachedValueProvider.Result.create(
                buildTemplateLookupElements(project),
                VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
            )
        }
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

    private fun buildTemplateLookupElements(project: Project): List<LookupElement> {
        val elements = HashMap<String, LookupElement>()
        val index = FileBasedIndex.getInstance()
        val scope = GlobalSearchScope.allScope(project)

        index.processAllKeys(ShopwareTemplateIndex.key, { viewPath ->
            index.getContainingFiles(ShopwareTemplateIndex.key, viewPath, scope).forEach { file ->
                val bundleName = getBundleNameForPath(file.path) ?: return@forEach
                val reference = "@$bundleName/$viewPath"

                elements.putIfAbsent(
                    reference,
                    LookupElementBuilder.create(reference).withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )
            }

            true
        }, project)

        return elements.values.toList()
    }

    private fun getTemplatesByViewPath(project: Project, templatePath: String): List<VirtualFile> {
        return FileBasedIndex.getInstance().getContainingFiles(
            ShopwareTemplateIndex.key,
            templatePath,
            GlobalSearchScope.allScope(project)
        ).filter { it.path.endsWith("Resources/views/$templatePath") }
    }

    private fun filterByBundle(
        candidates: List<VirtualFile>,
        templatePath: String,
        bundleName: String
    ): List<VirtualFile> {
        val suffix = "Resources/views/$templatePath"
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
