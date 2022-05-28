package de.shyim.shopware6.inspection.quickfix.php

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.impl.NewExpressionImpl

class CriteriaIdMisusedFix: LocalQuickFix {
    override fun getFamilyName() = "Move ID filter to Criteria constructor"
    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        if (descriptor.psiElement !is MethodReference) {
            return
        }

        val methodRef: MethodReference = descriptor.psiElement as MethodReference

        CommandProcessor.getInstance().executeCommand(project, {
            val criteriaConstructors: MutableList<NewExpressionImpl> = ArrayList()

            methodRef.parent.parent.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is NewExpressionImpl && element.classReference !== null && element.classReference!!.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria") {
                        criteriaConstructors.add(element)
                        return
                    }

                    super.visitElement(element)
                }
            })

            if (criteriaConstructors.size == 0) {
                Messages.showInfoMessage("Cannot find Criteria constructor in current context", "Info")
                return@executeCommand
            }

            if (criteriaConstructors.size != 1) {
                Messages.showInfoMessage("Found multiple Criteria constructor. Cannot modify it", "Info")
                return@executeCommand
            }

            val filterArgument: PsiElement = (methodRef.parameters.first() as NewExpressionImpl).parameters[1]

            if ((methodRef.parameters.first() as NewExpressionImpl).classReference?.type.toString() == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Filter\\EqualsFilter") {
                val newFile = PsiFileFactory.getInstance(project)
                    .createFileFromText("test.php", PhpFileType.INSTANCE, "<?php return [1];")
                newFile.firstChild.children[0].children[0].children[0].children[0].replace(filterArgument)

                criteriaConstructors.first().parameterList?.add(newFile.firstChild.children[0].children[0])
            } else {
                criteriaConstructors.first().parameterList?.add(filterArgument)
            }

            descriptor.psiElement.parent.delete()
        }, "Changing Constructor", null)
    }
}