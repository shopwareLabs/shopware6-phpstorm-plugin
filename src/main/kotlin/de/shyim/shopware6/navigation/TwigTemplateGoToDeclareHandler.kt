package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.util.elementType
import com.jetbrains.twig.TwigTokenTypes
import com.jetbrains.twig.elements.TwigElementTypes
import de.shyim.shopware6.util.ShopwareTemplateUtil

class TwigTemplateGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (element == null || element.elementType != TwigTokenTypes.STRING_TEXT) {
            return null
        }

        val tag = element.parent ?: return null

        if (tag.elementType != TwigElementTypes.TAG) {
            return null
        }

        val tagName = tag.node.findChildByType(TwigTokenTypes.TAG_NAME)?.text
        if (tagName != "sw_extends" && tagName != "sw_include") {
            return null
        }

        // only the template reference, not other strings inside the tag (e.g. sw_include with)
        if (tag.node.getChildren(null).firstOrNull { it.elementType == TwigTokenTypes.STRING_TEXT }?.psi != element) {
            return null
        }

        val project = element.project
        val currentPath = element.containingFile.originalFile.virtualFile?.path

        val targets = ShopwareTemplateUtil.resolveTemplateReference(project, element.text)
            .filter { it.path != currentPath }
            .mapNotNull { PsiManager.getInstance(project).findFile(it) }

        return if (targets.isEmpty()) null else targets.toTypedArray()
    }
}
