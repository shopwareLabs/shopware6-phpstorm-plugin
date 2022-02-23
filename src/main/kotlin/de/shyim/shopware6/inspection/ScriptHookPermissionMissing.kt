package de.shyim.shopware6.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiReference
import com.jetbrains.twig.TwigFile
import com.jetbrains.twig.elements.TwigFieldReference
import com.jetbrains.twig.elements.TwigVariableReference
import de.shyim.shopware6.util.ScriptHookUtil

class ScriptHookPermissionMissing : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile || file.parent?.parent?.name != "scripts") {
            return super.buildVisitor(holder, isOnTheFly)
        }

        val hook = ScriptHookUtil.getHookByName(file.project, file.parent!!.name)
            ?: return super.buildVisitor(holder, isOnTheFly)

        val availableServices: MutableList<String> = mutableListOf()
        val allFacades = ScriptHookUtil.getAllFacades(file.project)

        for (service in hook.services) {
            if (allFacades[service] != null) {
                availableServices.add(allFacades[service]!!.name)
            }
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigFieldReference && element.firstChild is TwigVariableReference && element.firstChild.text == "services") {
                    val serviceName = element.nameIdentifier?.text

                    if (!availableServices.contains(serviceName)) {
                        holder.registerProblem(
                            element as PsiReference,
                            "The service $serviceName is not available in context of ${hook.name}",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }
            }
        }
    }
}