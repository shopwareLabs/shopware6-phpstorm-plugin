package de.shyim.shopware6.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Method

class ClassUsedInsteadOfAbstraction: LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is PhpFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object: PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is Method && element.name == "__construct") {
                    element.parameters.forEach { parameter ->
                        val sourceClasses =
                            PhpIndex.getInstance(holder.project).getClassesByFQN(parameter.declaredType.toString())

                        sourceClasses.forEach { sourceClass ->
                            sourceClass.superClasses.forEach { superClass ->
                                if (!superClass.isAbstract) {
                                    return
                                }

                                superClass.ownMethods.forEach { method ->
                                    if (method.name == "getDecorated") {
                                        holder.registerProblem(parameter, "The typehint should be ${superClass.fqn} to allow decoration", ProblemHighlightType.GENERIC_ERROR)
                                    }
                                }
                            }
                        }
                    }
                }

                super.visitElement(element)
            }
        }
    }
}