package de.shyim.shopware6.inspection.twig

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.inspection.quickfix.twig.AddMissingTwigVersioningCommentFix
import de.shyim.shopware6.util.TwigUtil

class TwigBlockHashMissing : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        // the upstream templates themselves don't need a versioning comment
        val virtualFile = holder.file.originalFile.virtualFile
        if (virtualFile != null && TwigUtil.isUpstreamTemplate(virtualFile.path)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        // only files extending another template override upstream blocks
        if (!TwigUtil.isExtendingTemplate(holder.file)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        val filePath = virtualFile?.path ?: return super.buildVisitor(holder, isOnTheFly)
        val chainPaths = TwigUtil.getExtendsChainPaths(holder.file)

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null && TwigUtil.getShopwareBlockComment(element) === null) {
                    if (TwigUtil.getUpstreamBlocks(element.project, filePath, chainPaths, element.name!!).isEmpty()) {
                        return
                    }

                    holder.registerProblem(
                        element.parent,
                        "The block does not have a versioning comment",
                        ProblemHighlightType.WARNING,
                        AddMissingTwigVersioningCommentFix()
                    )
                }
            }
        }
    }
}