package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import de.shyim.shopware6.symfonyplugin.ShopwareTranslationProvider
import de.shyim.shopware6.util.PHPPattern

class PHPSnippetGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (element == null || editor == null) {
            return null
        }

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (PHPPattern.isShopwareStorefrontControllerTrans(element)) {
            val provider = ShopwareTranslationProvider()
            psiElements.addAll(provider.getTranslationTargets(editor.project!!, element.text, ""))
        }

        return psiElements.toTypedArray()
    }
}