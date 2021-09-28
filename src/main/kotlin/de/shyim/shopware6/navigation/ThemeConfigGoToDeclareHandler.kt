package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import de.shyim.shopware6.util.ThemeConfigUtil
import de.shyim.shopware6.util.TwigPattern

class ThemeConfigGoToDeclareHandler : GotoDeclarationHandler {
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

        if (TwigPattern.getPrintBlockOrTagFunctionPattern("theme_config").accepts(element)) {
            val text = element.text.replace("\"", "").replace("'", "")

            ThemeConfigUtil.getAllConfigs(project).forEach {
                if (it.name == text) {
                    val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                    if (file != null) {
                        val psi = PsiManager.getInstance(project).findFile(file)

                        if (psi != null) {
                            psiElements.add(ThemeConfigUtil.getTargets(psi, text))
                        }
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}