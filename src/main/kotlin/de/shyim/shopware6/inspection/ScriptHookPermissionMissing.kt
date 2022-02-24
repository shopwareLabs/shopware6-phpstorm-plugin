package de.shyim.shopware6.inspection

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.jetbrains.twig.TwigFile
import de.shyim.shopware6.index.dict.ShopwareApp
import de.shyim.shopware6.util.ShopwareAppUtil
import de.shyim.shopware6.util.TwigPattern

class ScriptHookPermissionMissing : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile || file.parent?.parent?.name != "scripts") {
            return super.buildVisitor(holder, isOnTheFly)
        }

        val app = getAppByTwigFile(file) ?: return super.buildVisitor(holder, isOnTheFly)

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (TwigPattern.getScriptRepositorySearchPattern().accepts(element)) {
                    if (!app.permissions.contains("${element.text}:read")) {
                        holder.registerProblem(
                            element,
                            "This app doesn't have access to entity ${element.text}. Add the permission to your manifest.xml",
                            ProblemHighlightType.GENERIC_ERROR
                        )
                    }
                }
            }
        }
    }

    private fun getAppByTwigFile(file: TwigFile): ShopwareApp? {
        val appFolderName = file.parent?.parent?.parent?.parent?.name ?: return null

        return ShopwareAppUtil.getAppByFolderName(appFolderName, file.project)
    }
}