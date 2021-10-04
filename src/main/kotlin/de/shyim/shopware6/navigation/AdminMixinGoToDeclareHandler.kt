package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import de.shyim.shopware6.util.AdminMixinUtil
import de.shyim.shopware6.util.JavaScriptPattern

class AdminMixinGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (editor === null || editor.project === null || element == null) {
            return null
        }

        val project = editor.project!!

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (JavaScriptPattern.getMixinGetByName().accepts(element)) {
            val text = element.text.replace("\"", "").replace("'", "")

            psiElements.addAll(AdminMixinUtil.getTargets(project, text))
        }

        return psiElements.toTypedArray()
    }
}