package de.shyim.shopware6.inspection.store.composer

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonObject
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile

class MissingComposerRequire : ExtensionComposerInspection() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!isExtensionComposerFile(holder)) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitFile(file: PsiFile) {
                if (file is JsonFile && file.topLevelValue is JsonObject) {
                    val root = (file.topLevelValue as JsonObject)

                    val require = root.findProperty("require")

                    if (require == null) {
                        holder.registerProblem(root, "Store: An composer require to shopware/core is required")
                    } else if (require.lastChild !is JsonObject) {
                        holder.registerProblem(require, "Store: The require in composer.json needs to be an object")
                    } else if ((require.lastChild as JsonObject).findProperty("shopware/core") == null) {
                        holder.registerProblem(require, "Store: An composer require to shopware/core is required")
                    }
                }

                super.visitFile(file)
            }
        }
    }
}