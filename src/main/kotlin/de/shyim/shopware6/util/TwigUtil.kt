package de.shyim.shopware6.util

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.composer.actions.update.ComposerInstalledPackagesService
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockTag
import com.jetbrains.twig.elements.TwigComment
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.index.dict.TwigBlockHash
import org.codehaus.jettison.json.JSONException
import org.codehaus.jettison.json.JSONObject

object TwigUtil {
    fun getTemplatePathByFilePath(filePath: String, project: Project): String? {
        var path: String? = null
        ShopwareBundleUtil.getAllBundlesRelatedToViews(project).forEach { bundle ->
            if (filePath.startsWith(bundle.viewPath)) {
                path = filePath.replace(bundle.viewPath, "")
            }
        }

        return path
    }

    fun getBundleByFilePath(filePath: String, project: Project): String? {
        var name: String? = null
        ShopwareBundleUtil.getAllBundlesRelatedToViews(project).forEach { bundle ->
            if (filePath.startsWith(bundle.viewPath)) {
                name = bundle.name
            }
        }

        return name
    }

    fun createVirtualTwigFile(project: Project, comment: String): PsiElement {
        val newFile = PsiFileFactory.getInstance(project)
            .createFileFromText("test.twig", TwigFileType.INSTANCE, comment)

        return newFile
    }

    fun getRelativePath(path: String): String {
        return path.substringAfter("Resources/views/")
    }

    fun isShopwareCoreTemplate(path: String): Boolean {
        return path.contains("src/Storefront/Resources/views/storefront") || path.contains("vendor/shopware/storefront/Resources/views/storefront")
    }

    fun isUpstreamTemplate(path: String): Boolean {
        return isShopwareCoreTemplate(path) || (path.contains("vendor/") && path.contains("Resources/views/"))
    }

    private val EXTENDS_PATTERN = Regex("\\{%-?\\s*(sw_)?extends\\s")
    private val EXTENDS_TARGET_PATTERN = Regex("\\{%-?\\s*(?:sw_)?extends\\s+['\"](@[A-Za-z0-9_]+/[^'\"]+)['\"]")

    fun isExtendingTemplate(file: PsiFile): Boolean {
        return EXTENDS_PATTERN.containsMatchIn(file.text)
    }

    fun findExtendsTargetReference(content: CharSequence): String? {
        return EXTENDS_TARGET_PATTERN.find(content)?.groupValues?.get(1)
    }

    fun getExtendsChainPaths(file: PsiFile): List<String> {
        // cached per file, as every block of a template walks the same chain
        return CachedValuesManager.getCachedValue(file) {
            CachedValueProvider.Result.create(
                // the first target is read from the PSI so that unsaved edits are respected
                ShopwareTemplateUtil.followExtendsChain(
                    file.project,
                    file.originalFile.virtualFile?.path,
                    findExtendsTargetReference(file.text)
                ),
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    fun findBlockTagsInFile(project: Project, path: String, blockName: String): List<PsiElement> {
        val virtualFile = ShopwareTemplateUtil.findTemplateByPath(project, path) ?: return emptyList()
        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return emptyList()

        val blockTags = ArrayList<PsiElement>()

        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name == blockName) {
                    blockTags.add(element)
                    return
                }

                super.visitElement(element)
            }
        })

        return blockTags
    }

    /**
     * Paths of the templates extending the given file, nearest child first. Cached per file, as
     * every block of a template resolves the same set.
     */
    fun getExtendingTemplatePaths(file: PsiFile): List<String> {
        return CachedValuesManager.getCachedValue(file) {
            val virtualFile = file.originalFile.virtualFile

            CachedValueProvider.Result.create(
                if (virtualFile == null) {
                    emptyList()
                } else {
                    ShopwareTemplateUtil.getTemplatesExtendingTemplate(file.project, virtualFile)
                        .map { it.path }
                },
                PsiModificationTracker.MODIFICATION_COUNT
            )
        }
    }

    /**
     * Paths of the templates extending the given file which define a block of the given name,
     * nearest child first. Resolved from the indexes only, so it is cheap enough to decide
     * whether a gutter marker is shown.
     */
    fun getDownstreamBlockPaths(file: PsiFile, blockName: String): List<String> {
        val extending = getExtendingTemplatePaths(file)

        if (extending.isEmpty()) {
            return emptyList()
        }

        val paths = FileBasedIndex.getInstance()
            .getValues(TwigBlockHashIndex.key, blockName, GlobalSearchScope.allScope(file.project))
            .mapTo(HashSet()) { it.absolutePath }

        return extending.filter { paths.contains(it) }
    }

    /**
     * Every block of the given name in templates extending the given file, nearest child first
     */
    fun getDownstreamBlocks(file: PsiFile, blockName: String): List<PsiElement> {
        val project = file.project

        return getDownstreamBlockPaths(file, blockName).flatMap { findBlockTagsInFile(project, it, blockName) }
    }

    fun getUpstreamBlocks(file: PsiFile, blockName: String): List<TwigBlockHash> {
        val filePath = file.originalFile.virtualFile?.path ?: return emptyList()

        return getUpstreamBlocks(file.project, filePath, getExtendsChainPaths(file), blockName)
    }

    fun getUpstreamBlocks(
        project: Project,
        filePath: String,
        chainPaths: List<String>,
        blockName: String
    ): List<TwigBlockHash> {
        val values = FileBasedIndex.getInstance()
            .getValues(TwigBlockHashIndex.key, blockName, GlobalSearchScope.allScope(project))

        // the template chain declared via sw_extends is the most reliable upstream information,
        // ordered nearest parent first
        val chainBlocks = chainPaths.mapNotNull { path -> values.firstOrNull { it.absolutePath == path } }
        if (chainBlocks.isNotEmpty()) {
            return chainBlocks
        }

        // fallback for blocks not found in the chain (e.g. injected by another override of the
        // same template): same relative path in another file. Blocks tracking an upstream
        // themselves (versioning comment) are overrides, never upstream
        return values
            .filter { it.relativePath == getRelativePath(filePath) && it.absolutePath != filePath && !it.hasVersioningComment }
            .sortedWith(
                compareByDescending<TwigBlockHash> { isShopwareCoreTemplate(it.absolutePath) }
                    .thenByDescending { isUpstreamTemplate(it.absolutePath) }
                    .thenBy { it.absolutePath }
            )
    }

    fun getComposerPackageByPath(path: String): String? {
        if (!path.contains("vendor/")) {
            return null
        }

        val parts = path.substringAfterLast("vendor/").split("/")

        if (parts.size < 3) {
            return null
        }

        return "${parts[0]}/${parts[1]}"
    }

    fun getShopwareBlockComment(element: PsiElement?): PsiElement? {
        if (element == null) {
            return null
        }

        val blockTag = if (element is TwigBlockTag) {
            element
        } else if (element.parent is TwigBlockTag) {
            element.parent as TwigBlockTag
        } else {
            return null
        }

        val commentElement = blockTag.parent.prevSibling?.prevSibling
        return if (commentElement is TwigComment && commentElement.text.contains("{# shopware-block:")) {
            commentElement
        } else {
            null
        }
    }

    fun extractShopwareBlockData(element: PsiElement): ShopwareBlockData? {
        val parts = element.text.replace("{# shopware-block: ", "").replace(" #}", "").trim().split("@")

        if (parts.size != 2) {
            return null
        }

        return ShopwareBlockData(parts[0], parts[1])
    }

    fun addVersioningComment(
        blockTag: TwigBlockTag,
        templatePath: String
    ) {
        val commentBlock = getShopwareBlockComment(blockTag)

        val commentTag = getVersioningComment(
            blockTag.project,
            blockTag.name!!,
            templatePath,
            blockTag.containingFile.originalFile
        ) ?: return

        CommandProcessor.getInstance().executeCommand(blockTag.project, {
            if (commentBlock != null) {
                commentBlock.replace(createVirtualTwigFile(blockTag.project, commentTag).firstChild)
            } else {
                val createTwigVersioningComment = createVirtualTwigFile(blockTag.project, commentTag)
                blockTag.parent.parent.addAfter(createTwigVersioningComment.firstChild, blockTag.parent.prevSibling)
                blockTag.parent.parent.addAfter(createTwigVersioningComment.lastChild, blockTag.parent.prevSibling)
            }
        }, "Add Twig Versioning Comment", null)
    }

    fun getVersioningComment(
        project: Project,
        blockName: String,
        templatePath: String,
        sourceFile: PsiFile? = null
    ): String? {
        val upstreamBlock = (if (sourceFile != null) {
            getUpstreamBlocks(sourceFile, blockName)
        } else {
            // no source file known (e.g. the override is being created): fall back to the
            // relative path. Blocks carrying a versioning comment themselves are overrides,
            // never upstream. Prefer the Shopware core template and sort by path so the
            // recorded hash is deterministic
            FileBasedIndex.getInstance()
                .getValues(TwigBlockHashIndex.key, blockName, GlobalSearchScope.allScope(project))
                .filter { it.relativePath == templatePath && !it.hasVersioningComment }
                .sortedWith(
                    compareByDescending<TwigBlockHash> { isShopwareCoreTemplate(it.absolutePath) }
                        .thenByDescending { isUpstreamTemplate(it.absolutePath) }
                        .thenBy { it.absolutePath }
                )
        }).firstOrNull() ?: return null

        val packageVersion = getUpstreamPackageVersion(project, upstreamBlock.absolutePath)

        var commentText = upstreamBlock.hash

        if (packageVersion != null) {
            commentText += "@${packageVersion}"
        }

        return "{# shopware-block: $commentText #}\n"
    }

    private fun getUpstreamPackageVersion(project: Project, path: String): String? {
        val packageName = getComposerPackageByPath(path)
        if (packageName != null) {
            return ComposerInstalledPackagesService.getInstance(project, project.guessProjectDir())
                ?.getCurrentPackageVersion(packageName)
        }

        // extensions in custom/plugins: read the version from the extension's composer.json
        val templateFile = ShopwareTemplateUtil.findTemplateByPath(project, path) ?: return null

        var dir = templateFile.parent
        while (dir != null) {
            val composerJson = dir.findChild("composer.json")
            if (composerJson != null) {
                return try {
                    JSONObject(composerJson.contentsToByteArray().toString(Charsets.UTF_8))
                        .optString("version")
                        .takeIf { it.isNotEmpty() }
                } catch (e: JSONException) {
                    null
                }
            }

            dir = dir.parent
        }

        return null
    }
}

class ShopwareBlockData(val hash: String, val version: String)