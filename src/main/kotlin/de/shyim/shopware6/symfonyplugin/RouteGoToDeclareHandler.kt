package de.shyim.shopware6.symfonyplugin

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import de.shyim.shopware6.util.TwigPattern
import fr.adrienbrault.idea.symfony2plugin.routing.RouteHelper

class RouteGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (editor === null || editor.project === null || element == null) {
            return null
        }

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (TwigPattern.getPrintBlockOrTagFunctionPattern("seoUrl", "sw_csrf").accepts(element)) {
            psiElements.addAll(RouteHelper.getRouteDefinitionTargets(editor.project!!, element.text))
        }

        return psiElements.toTypedArray()
    }
}