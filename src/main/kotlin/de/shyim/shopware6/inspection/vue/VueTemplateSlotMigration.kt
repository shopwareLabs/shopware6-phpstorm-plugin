package de.shyim.shopware6.inspection.vue

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlTag
import com.jetbrains.twig.TwigFile
import de.shyim.shopware6.inspection.quickfix.vue.VueTemplateSlotMigrationFix

class VueTemplateSlotMigration : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is XmlAttribute && isTemplateTagWithSlotAttribute().accepts(element)) {
                    holder.registerProblem(
                        element,
                        "The slot attribute is deprecated. Please use the new slot syntax <template #${element.value}>",
                        ProblemHighlightType.WARNING,
                        VueTemplateSlotMigrationFix()
                    )
                }

                super.visitElement(element)
            }
        }
    }

    private fun isTemplateTagWithSlotAttribute(): PsiElementPattern.Capture<XmlAttribute> {
        return PlatformPatterns
            .psiElement(XmlAttribute::class.java)
            .withName("slot")
            .withParent(
                PlatformPatterns.psiElement(XmlTag::class.java)
                    .withName("template")
            )
    }
}