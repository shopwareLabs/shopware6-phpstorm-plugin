package de.shyim.shopware6.inspection.php

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.NewExpression
import com.jetbrains.php.lang.psi.elements.PhpTypedElement
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.shyim.shopware6.inspection.quickfix.php.CriteriaIdMisusedFix

class CriteriaIdMisused: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is PhpFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (
                    element is MethodReference &&
                    element.nameNode?.text == "addFilter" &&
                    element.firstChild is PhpTypedElement &&
                    (element.firstChild as PhpTypedElement).type.toString() == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria" &&
                    element.parameters.isNotEmpty() &&
                    element.parameters.first() is NewExpression
                ) {
                    val firstArgument = (element.parameters.first() as NewExpression)

                    if (firstArgument.type.toString() != "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Filter\\EqualsFilter" && firstArgument.type.toString() != "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Filter\\EqualsAnyFilter") {
                        return
                    }

                    if (firstArgument.parameters.isEmpty()) {
                        return
                    }

                    if (firstArgument.parameters.first() !is StringLiteralExpression) {
                        return
                    }

                    if ((firstArgument.parameters.first() as StringLiteralExpression).contents != "id") {
                        return
                    }

                    holder.registerProblem(
                        element as PsiElement,
                        "Criteria: IDs should be set in the constructor to not execute a additional query internally to find the IDs",
                        ProblemHighlightType.WARNING,
                        CriteriaIdMisusedFix()
                    )
                }

                super.visitElement(element)
            }
        }
    }
}