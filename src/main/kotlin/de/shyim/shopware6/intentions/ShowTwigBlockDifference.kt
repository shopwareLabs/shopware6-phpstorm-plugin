package de.shyim.shopware6.intentions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.composer.actions.update.ComposerInstalledPackagesService
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.util.TwigUtil
import java.net.HttpURLConnection
import java.net.URI

class ShowTwigBlockDifference : PsiElementBaseIntentionAction() {
    override fun getFamilyName() = "Show Twig block difference"
    override fun getText() = "Show Twig block difference"

    override fun startInWriteAction(): Boolean {
        return false
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null) {
            return false
        }

        val blockTag = if (element is TwigBlockTag) {
            element
        } else if (element.parent is TwigBlockTag) {
            element.parent as TwigBlockTag
        } else {
            return false
        }

        val existingComment = TwigUtil.getShopwareBlockComment(element) ?: return false
        val commentData = TwigUtil.extractShopwareBlockData(existingComment) ?: return false

        return FileBasedIndex.getInstance().getValues(
            TwigBlockHashIndex.key,
            blockTag.name!!,
            GlobalSearchScope.allScope(project)
        ).any {
            it.hash != commentData.hash && it.absolutePath != element.containingFile.virtualFile.path && it.relativePath == TwigUtil.getRelativePath(
                element.containingFile.virtualFile.path
            )
        }
    }

    override fun checkFile(file: PsiFile?): Boolean {
        return true
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val blockTag = if (element is TwigBlockTag) {
            element
        } else {
            element.parent as TwigBlockTag
        }

        val templatePath = editor?.virtualFile?.let { TwigUtil.getRelativePath(it.path) } ?: return

        val blockCommentData = TwigUtil.extractShopwareBlockData(blockTag.parent.prevSibling.prevSibling) ?: return

        val hash = FileBasedIndex.getInstance().getValues(
            TwigBlockHashIndex.key,
            blockTag.name!!,
            GlobalSearchScope.allScope(element.project)
        ).firstOrNull { it.relativePath == templatePath } ?: return

        val content = fetchTwigBlockContent(editor, blockCommentData.version, hash.relativePath) ?: return

        val shopwareVersion = ComposerInstalledPackagesService.getInstance(project, project.guessProjectDir())
            ?.getCurrentPackageVersion("shopware/storefront") ?: "unknown"

        val contentFactory = DiffContentFactory.getInstance()
        val current = contentFactory.create(project, hash.text, TwigFileType.INSTANCE)
        val previous = contentFactory.create(
            project,
            this.getBlockContentOutOfContent(content, blockTag.name!!, project)!!,
            TwigFileType.INSTANCE
        )

        DiffManager.getInstance().showDiff(
            project,
            SimpleDiffRequest(
                blockTag.name!!,
                previous,
                current,
                "Previous (${blockCommentData.version})",
                "Current ($shopwareVersion)"
            )
        )
    }

    private fun fetchTwigBlockContent(editor: Editor, version: String, path: String): String? {
        val url =
            URI("https://raw.githubusercontent.com/shopware/shopware/${version}/src/Storefront/Resources/views/${path}").toURL()

        try {
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "PhpStorm Shopware 6 Plugin")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val content = connection.inputStream.bufferedReader().use { it.readText() }
                connection.disconnect()

                return content
            } else {
                connection.disconnect()
                return null
            }
        } catch (e: Exception) {
            HintManager.getInstance()
                .showErrorHint(editor, "Could not fetch content from $url, due error: ${e.message}")
            return null
        }
    }

    private fun getBlockContentOutOfContent(content: String, blockName: String, project: Project): String? {
        val newFile = PsiFileFactory.getInstance(project)
            .createFileFromText("test.twig", TwigFileType.INSTANCE, content)

        var blockContent: String? = null

        newFile.acceptChildren(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name == blockName) {
                    blockContent = element.parent.text
                    return
                }

                super.visitElement(element)
            }
        })

        return blockContent
    }
}