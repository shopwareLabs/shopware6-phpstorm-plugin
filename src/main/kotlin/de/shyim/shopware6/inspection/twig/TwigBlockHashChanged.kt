package de.shyim.shopware6.inspection.twig

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.twig.TwigFile
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.util.TwigUtil

class TwigBlockHashChanged : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        val filePath = file.originalFile.virtualFile?.path ?: return super.buildVisitor(holder, isOnTheFly)
        val chainPaths = TwigUtil.getExtendsChainPaths(file)

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null && TwigUtil.getShopwareBlockComment(element) !== null) {
                    // the same block can exist multiple times upstream, e.g. when a third-party
                    // extension overrides a core template with the same relative path
                    val upstreamBlocks =
                        TwigUtil.getUpstreamBlocks(element.project, filePath, chainPaths, element.name!!)

                    if (upstreamBlocks.isEmpty()) {
                        return
                    }

                    var commentBlock =
                        TwigUtil.extractShopwareBlockData(element.parent.prevSibling?.prevSibling!!) ?: return

                    if (upstreamBlocks.none { it.hash == commentBlock.hash }) {
                        holder.registerProblem(
                            element.parent,
                            "The upstream block has been changed, please update the block",
                            ProblemHighlightType.WARNING
                        )
                    }
                }
            }
        }
    }
}