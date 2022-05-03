package de.shyim.shopware6.inspection.store.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class MissingComposerExtraManufacturerLink : ExtensionComposerInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!isExtensionComposerFile(holder)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is JsonProperty && element.name == "extra" && element.lastChild is JsonObject) {
                    val extraPart = (element.lastChild as JsonObject)

                    val manufacturerLink = extraPart.findProperty("manufacturerLink")

                    if (manufacturerLink == null) {
                        holder.registerProblem(
                            extraPart,
                            "Store: An manufacturerLink is required for an extension to work"
                        )
                        return
                    }

                    val manufacturerObject = manufacturerLink.lastChild

                    if (manufacturerObject is JsonObject) {
                        val deLabel = manufacturerObject.findProperty("de-DE")
                        val enLabel = manufacturerObject.findProperty("en-GB")

                        if (deLabel == null) {
                            holder.registerProblem(
                                manufacturerObject,
                                "Store: An manufacturerLink in language de-DE is required"
                            )
                        }

                        if (enLabel == null) {
                            holder.registerProblem(
                                manufacturerObject,
                                "Store: An manufacturerLink in language en-GB is required"
                            )
                        }
                    } else {
                        holder.registerProblem(
                            manufacturerLink,
                            "Store: The manufacturerLink property should be an object of language code to url"
                        )
                    }
                }
            }
        }
    }
}