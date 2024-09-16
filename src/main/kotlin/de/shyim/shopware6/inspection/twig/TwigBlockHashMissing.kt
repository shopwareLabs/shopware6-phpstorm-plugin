package de.shyim.shopware6.inspection.twig

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.inspection.quickfix.twig.AddMissingTwigVersioningCommentFix
import de.shyim.shopware6.util.TwigUtil

class TwigBlockHashMissing : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null && TwigUtil.getShopwareBlockComment(element) === null) {
                    if (! FileBasedIndex.getInstance().getValues(
                            TwigBlockHashIndex.key,
                            element.name!!,
                            GlobalSearchScope.allScope(element.project)
                        )
                            .any { it.relativePath == TwigUtil.getRelativePath(element.containingFile.originalFile.virtualFile.path) }) {
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