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
                    val hash = FileBasedIndex.getInstance().getValues(
                        TwigBlockHashIndex.key,
                        element.name!!,
                        GlobalSearchScope.allScope(element.project)
                    )
                        .firstOrNull { it.relativePath == TwigUtil.getRelativePath(element.containingFile.originalFile.virtualFile.path) }
                        ?: return

                    var commentBlock =
                        TwigUtil.extractShopwareBlockData(element.parent.prevSibling?.prevSibling!!) ?: return

                    if (hash.hash != commentBlock.hash) {
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