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
import de.shyim.shopware6.index.TwigBlockDeprecationIndex
import de.shyim.shopware6.util.TwigUtil

class TwigBlockDeprecated : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null) {
                    FileBasedIndex.getInstance().getValues(
                        TwigBlockDeprecationIndex.key,
                        element.name!!,
                        GlobalSearchScope.allScope(element.project)
                    ).forEach { deprecation ->
                        if (
                        // same relative path
                            deprecation.relPath == TwigUtil.getRelativePath(element.containingFile.originalFile.virtualFile.path)
                        ) {
                            holder.registerProblem(
                                element,
                                "This block is deprecated: ${deprecation.message}",
                                ProblemHighlightType.LIKE_DEPRECATED
                            )
                        }
                    }
                }
            }
        }
    }
}