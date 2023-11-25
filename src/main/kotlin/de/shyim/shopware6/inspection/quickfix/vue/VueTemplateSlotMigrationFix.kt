package de.shyim.shopware6.inspection.quickfix.vue

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.xml.XmlAttribute

class VueTemplateSlotMigrationFix : LocalQuickFix {
    override fun getFamilyName() = "Migrate old slot syntax to new one (works in Vue2 and Vue3)"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val xmlAttribute: XmlAttribute = descriptor.psiElement as XmlAttribute
        var tag = "#${xmlAttribute.value}"

        xmlAttribute.parent.children.forEach {
            if (it is XmlAttribute && it.name == "slot-scope") {
                tag = "${tag}=\"${it.value}\""
            }
        }

        CommandProcessor.getInstance().executeCommand(project, {
            val newFile = PsiFileFactory.getInstance(project)
                .createFileFromText("test.html", HtmlFileType.INSTANCE, "<template ${tag}></template>")

            xmlAttribute.parent.children.forEach {
                if (it is XmlAttribute && it.name == "slot-scope") {
                    // children 2 is PsiWhitespaceElement
                    it.delete()
                }
            }

            xmlAttribute.replace(newFile.firstChild.lastChild.children[3])
        }, "Migrating Syntax", null)
    }
}