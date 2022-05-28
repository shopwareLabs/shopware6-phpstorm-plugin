package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import de.shyim.shopware6.util.AdminModuleUtil
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.StringUtil

class AdminModuleGoToDeclareHandler : GotoDeclarationHandler {
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

        if (JavaScriptPattern.getRouteCompletion().accepts(element)) {
            val text = StringUtil.stripQuotes(element.text)

            psiElements.addAll(AdminModuleUtil.getRoutePsiTargets(text, project))
        }

        return psiElements.toTypedArray()
    }
}