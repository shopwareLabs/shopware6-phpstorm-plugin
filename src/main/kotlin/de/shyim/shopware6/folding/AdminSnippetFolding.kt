package de.shyim.shopware6.folding

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import de.shyim.shopware6.util.AdminSnippetUtil
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.StringUtil

class AdminSnippetFolding : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        val snippets = AdminSnippetUtil.getAllSnippetValues(root.project)

        root.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (JavaScriptPattern.getTranslationPattern().accepts(element)) {
                    val key = StringUtil.stripQuotes(element.text)

                    descriptors.add(
                        FoldingDescriptor(
                            element.parent.node,
                            TextRange(element.parent.textRange.startOffset, element.parent.textRange.endOffset),
                            null,
                            if (snippets.containsKey(key)) snippets[key]!! else "..."
                        )
                    )

                    return
                }

                super.visitElement(element)
            }
        })

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}