package de.shyim.shopware6.inspection.store.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class MissingComposerExtraSupportLink : ExtensionComposerInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!isExtensionComposerFile(holder)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is JsonProperty && element.name == "extra" && element.lastChild is JsonObject) {
                    val extraPart = (element.lastChild as JsonObject)

                    val supportLink = extraPart.findProperty("supportLink")

                    if (supportLink == null) {
                        holder.registerProblem(extraPart, "Store: An supportLink is required for an extension to work")
                        return
                    }

                    val supportObject = supportLink.lastChild

                    if (supportObject is JsonObject) {
                        val deLabel = supportObject.findProperty("de-DE")
                        val enLabel = supportObject.findProperty("en-GB")

                        if (deLabel == null) {
                            holder.registerProblem(supportObject, "Store: An supportLink in language de-DE is required")
                        }

                        if (enLabel == null) {
                            holder.registerProblem(supportObject, "Store: An supportLink in language en-GB is required")
                        }
                    } else {
                        holder.registerProblem(
                            supportLink,
                            "Store: The supportLink property should be an object of language code to url"
                        )
                    }
                }
            }
        }
    }
}