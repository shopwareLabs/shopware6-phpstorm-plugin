package de.shyim.shopware6.action.context

import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.PsiFile
import com.intellij.ui.components.JBList
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.twig.TwigFile
import de.shyim.shopware6.completion.SnippetCompletionElement
import de.shyim.shopware6.util.AdminSnippetUtil
import de.shyim.shopware6.util.FrontendSnippetUtil
import icons.ShopwareToolBoxIcons
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

class InsertSnippetAction : DumbAwareAction("Insert Snippet", "Insert snippet code", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val pf: PsiFile = LangDataKeys.PSI_FILE.getData(e.dataContext) ?: return
        val editor = LangDataKeys.EDITOR.getData(e.dataContext) ?: return

        val items: MutableList<SnippetCompletionElement> = if (pf.virtualFile.path.contains("app/administration")) {
            AdminSnippetUtil.getAllEnglishKeys(pf.project)
        } else {
            FrontendSnippetUtil.getAllEnglishKeys(pf.project)
        }

        val snippetList = JBList(items)
        snippetList.cellRenderer = object : JBList.StripedListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                if (renderer is JLabel && value is SnippetCompletionElement) {
                    var text = value.value

                    if (text.length > 10) {
                        text = text.substring(0, 10)
                    }

                    renderer.text = String.format("%s (%s)", text, value.key)
                }

                return renderer
            }
        }

        PopupChooserBuilder(snippetList)
            .setTitle("Shopware: Select Snippet")
            .setFilteringEnabled {
                return@setFilteringEnabled (it as SnippetCompletionElement).key + " " + it.value
            }
            .setItemChosenCallback(Runnable {
                if (snippetList.selectedValue == null) {
                    return@Runnable
                }

                ApplicationManager.getApplication().runWriteAction {
                    CommandProcessor.getInstance().executeCommand(pf.project, {
                        editor
                            .document
                            .insertString(
                                editor.caretModel.offset,
                                createFileBasedSnippet(pf, snippetList.selectedValue!!.key)
                            )
                    }, "Insert Snippet", null)
                }
            })
            .createPopup()
            .showInBestPositionFor(e.dataContext)
    }

    private fun createFileBasedSnippet(pf: PsiFile, key: String): String {
        if (pf is TwigFile && pf.virtualFile.path.contains("app/administration")) {
            return String.format("{{ \$tc('%s') }}", key)
        }

        if (pf is TwigFile) {
            return String.format("{{ \"%s\"|trans|sw_sanitize }}", key)
        }

        if (pf is JSFile) {
            return String.format("this.\$tc('%s')", key)
        }

        return String.format("\$this->trans('%s');", key)
    }

    override fun update(e: AnActionEvent) {
        val pf: PsiFile? = LangDataKeys.PSI_FILE.getData(e.dataContext)

        if (pf == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = pf is TwigFile || pf is PhpFile || pf is JSFile
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
}
