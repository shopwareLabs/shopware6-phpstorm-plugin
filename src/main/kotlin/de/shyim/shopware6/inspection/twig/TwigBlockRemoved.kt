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

class TwigBlockRemoved : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null && TwigUtil.getShopwareBlockComment(element) !== null) {
                    val upstreamBlocks = FileBasedIndex.getInstance().getValues(
                        TwigBlockHashIndex.key,
                        element.name!!,
                        GlobalSearchScope.allScope(element.project)
                    )

                    if (upstreamBlocks.any { it.relativePath == TwigUtil.getRelativePath(element.containingFile.originalFile.virtualFile.path) }) {
                        return
                    }

                    // without any indexed upstream templates every block would be reported as removed
                    if (FileBasedIndex.getInstance().getAllKeys(TwigBlockHashIndex.key, element.project).isEmpty()) {
                        return
                    }

                    if (upstreamBlocks.isEmpty()) {
                        holder.registerProblem(
                            element.parent,
                            "The upstream block has been removed, please check if your override is still needed",
                            ProblemHighlightType.WARNING
                        )
                    } else {
                        holder.registerProblem(
                            element.parent,
                            "The upstream block has been removed from this template, but still exists in: ${
                                upstreamBlocks.map { it.relativePath }.distinct().joinToString(", ")
                            }",
                            ProblemHighlightType.WARNING
                        )
                    }
                }
            }
        }
    }
}
