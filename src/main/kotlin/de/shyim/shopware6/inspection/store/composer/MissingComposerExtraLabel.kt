package de.shyim.shopware6.inspection.store.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class MissingComposerExtraLabel : ExtensionComposerInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!isExtensionComposerFile(holder)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is JsonProperty && element.name == "extra" && element.lastChild is JsonObject) {
                    val extraPart = (element.lastChild as JsonObject)

                    val label = extraPart.findProperty("label")

                    if (label == null) {
                        holder.registerProblem(extraPart, "Store: Label is required for an extension to work")
                        return
                    }

                    val labelObject = label.lastChild

                    if (labelObject is JsonObject) {
                        val deLabel = labelObject.findProperty("de-DE")
                        val enLabel = labelObject.findProperty("en-GB")

                        if (deLabel == null) {
                            holder.registerProblem(labelObject, "Store: Label in language de-DE is required")
                        }

                        if (enLabel == null) {
                            holder.registerProblem(labelObject, "Store: Label in language en-GB is required")
                        }
                    } else {
                        holder.registerProblem(
                            label,
                            "Store: Label property should be an object of language code to label"
                        )
                    }
                }
            }
        }
    }
}