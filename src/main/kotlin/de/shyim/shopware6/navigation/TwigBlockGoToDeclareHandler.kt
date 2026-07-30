package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.util.TwigUtil

class TwigBlockGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (element == null) {
            return null
        }

        val blockTag = element.parent as? TwigBlockTag ?: return null
        val blockName = blockTag.name ?: return null

        if (blockName != element.text) {
            return null
        }

        // navigate to the upstream block this one overrides, nearest parent first
        val targets = TwigUtil.getUpstreamBlocks(element.containingFile.originalFile, blockName)
            .mapNotNull { TwigUtil.findBlockTagInFile(element.project, it.absolutePath, blockName) }

        return if (targets.isEmpty()) null else targets.toTypedArray()
    }
}
