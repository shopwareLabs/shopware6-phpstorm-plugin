package de.shyim.shopware6.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.util.TwigUtil

class AddTwigVersioningIntention: PsiElementBaseIntentionAction() {
    override fun getFamilyName() = "Add/Update the Shopware 6 Twig versioning comment"
    override fun getText() = "Add/Update the Shopware 6 versioning comment"

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

        val found = FileBasedIndex.getInstance().getValues(TwigBlockHashIndex.key, blockTag.name!!, GlobalSearchScope.allScope(project)).firstOrNull { it.relativePath == TwigUtil.getRelativePath(element.containingFile.virtualFile.path) && it.absolutePath != element.containingFile.virtualFile.path } ?: return false

        // When not comment there, offer to create one
        val existingComment = TwigUtil.getShopwareBlockComment(element) ?: return true
        // Invalid comment, offer to create one
        val commentData = TwigUtil.extractShopwareBlockData(existingComment) ?: return true

        return commentData.hash != found.hash
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
        TwigUtil.addVersioningComment(blockTag, templatePath)
    }
}