package de.shyim.shopware6.util

import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.composer.actions.update.ComposerInstalledPackagesService
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockTag
import com.jetbrains.twig.elements.TwigComment
import de.shyim.shopware6.index.TwigBlockHashIndex

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

        val commentTag = getVersioningComment(blockTag.project, blockTag.name!!, templatePath) ?: return

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

    fun getVersioningComment(project: Project, blockName: String, templatePath: String): String? {
        val hash = (FileBasedIndex.getInstance()
            .getValues(TwigBlockHashIndex.key, blockName, GlobalSearchScope.allScope(project))
            .firstOrNull { it.relativePath == templatePath } ?: return null).hash

        val shopwareVersion = ComposerInstalledPackagesService.getInstance(project, project.guessProjectDir())
            ?.getCurrentPackageVersion("shopware/storefront")

        var commentText = hash;

        if (shopwareVersion != null) {
            commentText += "@${shopwareVersion}"
        }

        return "{# shopware-block: $commentText #}\n"
    }
}

class ShopwareBlockData (val hash: String, val version: String)