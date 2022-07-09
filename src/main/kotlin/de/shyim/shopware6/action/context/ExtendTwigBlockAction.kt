package de.shyim.shopware6.action.context

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.PsiFile
import com.jetbrains.twig.TwigFile
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.intentions.ExtendTwigBlockIntention
import icons.ShopwareToolBoxIcons

class ExtendTwigBlockAction: DumbAwareAction("Extend Twig block",  "Extend this Twig block in your extension", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val pf: PsiFile = LangDataKeys.PSI_FILE.getData(e.dataContext) ?: return
        val editor = LangDataKeys.EDITOR.getData(e.dataContext) ?: return
        val pe = pf.findElementAt(editor.caretModel.offset) ?: return

        ExtendTwigBlockIntention.extendSelectedTwigBlock(
            editor,
            pf.virtualFile,
            pe.project,
            pe
        )
    }

    override fun update(e: AnActionEvent) {
        val pf: PsiFile? = LangDataKeys.PSI_FILE.getData(e.dataContext)

        if (pf == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        if (pf !is TwigFile) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val editor = LangDataKeys.EDITOR.getData(e.dataContext)

        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val element = pf.findElementAt(editor.caretModel.offset)

        if (element == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = element is TwigBlockTag || element.parent is TwigBlockTag
    }
}