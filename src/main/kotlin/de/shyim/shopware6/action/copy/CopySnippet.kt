package de.shyim.shopware6.action.copy

import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBList
import icons.ShopwareToolBoxIcons
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

class CopySnippet : DumbAwareAction("Copy Snippet Code", "Copy the snippet code", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val pf: PsiFile = LangDataKeys.PSI_FILE.getData(e.dataContext) ?: return
        val editor = LangDataKeys.EDITOR.getData(e.dataContext) ?: return
        val pe = pf.findElementAt(editor.caretModel.offset) ?: return

        val key = resolveKey(pe)
        val collectionList: MutableCollection<String> = mutableListOf()
        collectionList.add("Admin Twig")
        collectionList.add("Admin JS")
        collectionList.add("Storefront Twig")

        val jbBundleList = JBList(collectionList)

        PopupChooserBuilder(jbBundleList)
            .setTitle("Target?")
            .setItemChoosenCallback {
                var code = ""

                when (jbBundleList.selectedValue!!) {
                    "Admin Twig" -> code = String.format("{{ \$tc('%s') }}", key)
                    "Admin JS" -> code = String.format("this.\$tc('%s')", key)
                    "Storefront Twig" -> code = String.format("{{ \"%s\"|trans }}", key)
                }

                Toolkit.getDefaultToolkit()
                    .systemClipboard
                    .setContents(
                        StringSelection(code),
                        null
                    )
            }
            .createPopup()
            .showInBestPositionFor(editor)
    }

    override fun update(e: AnActionEvent) {
        val pf: PsiFile? = LangDataKeys.PSI_FILE.getData(e.dataContext)

        if (pf == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = pf is JsonFile
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    private fun resolveKey(element: PsiElement): String {
        var cur = element
        var text = ""

        while (true) {
            if (cur is JsonFile) {
                return text
            }

            if (cur is JsonProperty) {
                text = if (text == "") {
                    cur.name
                } else {
                    "${cur.name}.${text}"
                }
            }

            cur = cur.parent
        }
    }
}