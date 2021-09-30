package de.shyim.shopware6.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import de.shyim.shopware6.util.AdminSnippetUtil
import de.shyim.shopware6.util.JavaScriptPattern

class AdministrationSnippetMissing : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is JSFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (JavaScriptPattern.getTranslationPattern().accepts(element)) {
                    val text = element.text.replace("\"", "").replace("'", "")

                    if (!AdminSnippetUtil.hasSnippet(text, element.project)) {
                        holder.registerProblem(
                            element,
                            "Could not find translation for this key",
                            ProblemHighlightType.WARNING
                        )
                    }
                }

                super.visitElement(element)
            }
        }
    }
}