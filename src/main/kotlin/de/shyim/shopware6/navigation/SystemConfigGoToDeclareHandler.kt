package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import de.shyim.shopware6.util.PHPPattern
import de.shyim.shopware6.util.SystemConfigUtil
import de.shyim.shopware6.util.TwigPattern

class SystemConfigGoToDeclareHandler : GotoDeclarationHandler {
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

        if (TwigPattern.getPrintBlockOrTagFunctionPattern("config")
                .accepts(element) || PHPPattern.isShopwareCoreSystemConfigServiceGetSingle(element)
        ) {
            val text = element.text.replace("\"", "").replace("'", "")

            SystemConfigUtil.getAllConfigs(project).forEach {
                if (it.namespace + "." + it.name == text) {
                    val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                    if (file != null) {
                        val psi = PsiManager.getInstance(project).findFile(file)

                        if (psi != null) {
                            psiElements.add(SystemConfigUtil.getTargets(psi, text.replace(it.namespace + ".", "")))
                        }
                    }
                }
            }
        }

        if (PHPPattern.isShopwareCoreSystemConfigServiceGetDomain(element)) {
            val text = element.text.replace("\"", "").replace("'", "")

            SystemConfigUtil.getAllConfigs(project).forEach {
                if (it.namespace == text && psiElements.size == 0) {
                    val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                    if (file != null) {
                        val psi = PsiManager.getInstance(project).findFile(file)

                        if (psi != null) {
                            psiElements.add(psi)
                        }
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}