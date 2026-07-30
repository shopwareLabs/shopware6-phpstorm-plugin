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
        val project = file.project
        val paths = ArrayList<String>()
        val visited = HashSet<String>()
        file.originalFile.virtualFile?.path?.let { visited.add(it) }

        var target = findExtendsTargetReference(file.text)

        while (target != null && paths.size < 10) {
            val bundleName = target.substring(1).substringBefore("/")
            val templatePath = target.substringAfter("/", "")

            if (templatePath.isEmpty()) {
                break
            }

            val parent = ShopwareTemplateUtil.findTemplateInBundle(project, bundleName, templatePath) ?: break

            if (!visited.add(parent.path)) {
                break
            }

            paths.add(parent.path)

            // the parents' extends targets come from the index, so no further files need to be loaded
            target = ShopwareTemplateUtil.getExtendsTarget(project, parent)
        }

        return paths
    }

    fun findBlockTagInFile(project: Project, path: String, blockName: String): PsiElement? {
        val virtualFile = ShopwareTemplateUtil.findTemplateByPath(project, path) ?: return null

        val psiFile = PsiManager.getInstance(project).findFile(virtualFile) ?: return null

        var blockTag: PsiElement? = null

        psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name == blockName) {
                    blockTag = element
                    stopWalking()
                    return
                }

                super.visitElement(element)
            }
        })

        return blockTag
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