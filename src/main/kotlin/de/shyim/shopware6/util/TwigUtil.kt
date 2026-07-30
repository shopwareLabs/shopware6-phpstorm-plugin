package de.shyim.shopware6.util

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.composer.actions.update.ComposerInstalledPackagesService
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockTag
import com.jetbrains.twig.elements.TwigComment
import de.shyim.shopware6.index.TwigBlockHashIndex
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

    fun isExtendingTemplate(file: PsiFile): Boolean {
        return EXTENDS_PATTERN.containsMatchIn(file.text)
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
            blockTag.containingFile.originalFile.virtualFile?.path
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
        excludePath: String? = null
    ): String? {
        // prefer the Shopware core template when multiple upstream templates contain the block
        val upstreamBlock = FileBasedIndex.getInstance()
            .getValues(TwigBlockHashIndex.key, blockName, GlobalSearchScope.allScope(project))
            .filter { it.relativePath == templatePath && it.absolutePath != excludePath }
            .sortedByDescending { isShopwareCoreTemplate(it.absolutePath) }
            .firstOrNull() ?: return null

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
        val templateFile = FilenameIndex.getVirtualFilesByName(
            path.substringAfterLast('/'),
            GlobalSearchScope.allScope(project)
        ).firstOrNull { it.path == path } ?: return null

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