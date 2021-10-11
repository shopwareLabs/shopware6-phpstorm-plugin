package de.shyim.shopware6.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.twig.TwigFile
import de.shyim.shopware6.inspection.quickfix.admin.AddAdministrationSnippetFix
import de.shyim.shopware6.util.AdminSnippetUtil
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.TwigPattern

class AdministrationSnippetMissing : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is JSFile && file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (JavaScriptPattern.getTranslationPattern().accepts(element) || TwigPattern.getTcPattern()
                        .accepts(element)
                ) {
                    val text = element.text.replace("\"", "").replace("'", "")

                    if (!AdminSnippetUtil.hasSnippet(text, element.project)) {
                        holder.registerProblem(
                            element,
                            "Could not find translation for this key",
                            ProblemHighlightType.WARNING,
                            AddAdministrationSnippetFix()
                        )
                    }
                }

                super.visitElement(element)
            }
        }
    }
}