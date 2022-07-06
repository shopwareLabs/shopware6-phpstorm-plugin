package de.shyim.shopware6.inspection.store.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import de.shyim.shopware6.util.StringUtil

class MissingComposerExtraDescription : ExtensionComposerInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!isExtensionComposerFile(holder)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is JsonProperty && element.name == "extra" && element.lastChild is JsonObject) {
                    val extraPart = (element.lastChild as JsonObject)

                    val description = extraPart.findProperty("description")

                    if (description == null) {
                        holder.registerProblem(extraPart, "Store: Description is required for the Shopware Store")
                        return
                    }

                    val descriptionObject = description.lastChild

                    if (descriptionObject is JsonObject) {
                        val deLabel = descriptionObject.findProperty("de-DE")
                        val enLabel = descriptionObject.findProperty("en-GB")

                        if (deLabel == null) {
                            holder.registerProblem(
                                descriptionObject,
                                "Store: Description in language de-DE is required"
                            )
                        } else if (deLabel.value is JsonStringLiteral) {
                            val text = StringUtil.stripQuotes((deLabel.value as JsonStringLiteral).text)

                            if (text.length < 150 || text.length > 185) {
                                holder.registerProblem(
                                    deLabel,
                                    "Store: Description should have a length from 150 up to 185 characters, " + text.length + " given."
                                )
                            }
                        }

                        if (enLabel == null) {
                            holder.registerProblem(
                                descriptionObject,
                                "Store: An description in language en-GB is required"
                            )
                        } else if (enLabel.value is JsonStringLiteral) {
                            val text = StringUtil.stripQuotes((enLabel.value as JsonStringLiteral).text)

                            if (text.length < 150 || text.length > 185) {
                                holder.registerProblem(
                                    enLabel,
                                    "Store: Description should have a length from 150 up to 185 characters, " + text.length + " given."
                                )
                            }
                        }
                    } else {
                        holder.registerProblem(
                            description,
                            "Store: The description property should be an object of language code to label"
                        )
                    }
                }
            }
        }
    }
}
