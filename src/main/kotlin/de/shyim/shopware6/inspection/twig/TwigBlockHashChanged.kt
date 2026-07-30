package de.shyim.shopware6.inspection.twig

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.twig.TwigFile
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.util.TwigUtil

class TwigBlockHashChanged : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null && TwigUtil.getShopwareBlockComment(element) !== null) {
                    // the same block can exist multiple times upstream, e.g. when a third-party
                    // extension overrides a core template with the same relative path
                    val upstreamBlocks = FileBasedIndex.getInstance().getValues(
                        TwigBlockHashIndex.key,
                        element.name!!,
                        GlobalSearchScope.allScope(element.project)
                    )
                        .filter { it.relativePath == TwigUtil.getRelativePath(element.containingFile.originalFile.virtualFile.path) }

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