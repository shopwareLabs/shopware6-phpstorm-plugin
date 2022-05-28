package de.shyim.shopware6.action.generator.app

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBList
import com.jetbrains.twig.TwigFileType
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.index.dict.ScriptHook
import de.shyim.shopware6.index.dict.ShopwareApp
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.util.PsiFolderUtil
import de.shyim.shopware6.util.ScriptHookUtil
import de.shyim.shopware6.util.ShopwareAppUtil
import icons.ShopwareToolBoxIcons
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

class NewAppScriptAction :
    DumbAwareAction("Create an App Script", "Create an new App scripts", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val hooks = ScriptHookUtil.getAllHooks(e.project!!)

        val jbHookList = JBList(hooks)

        jbHookList.cellRenderer = object : JBList.StripedListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                if (renderer is JLabel && value is ScriptHook) {
                    renderer.text = value.name
                }

                return renderer
            }
        }

        PopupChooserBuilder(jbHookList)
            .setTitle("Shopware: Select Hook")
            .setItemChoosenCallback {
                this.chooseApp(jbHookList.selectedValue!!, e.project!!)
            }
            .createPopup()
            .showInFocusCenter()
    }

    private fun chooseApp(scriptHook: ScriptHook, project: Project) {
        val apps = ShopwareAppUtil.getAllApps(project)
        val jbAppList = JBList(apps)

        jbAppList.cellRenderer = object : JBList.StripedListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                if (renderer is JLabel && value is ShopwareApp) {
                    renderer.text = value.name
                }

                return renderer
            }
        }

        PopupChooserBuilder(jbAppList)
            .setTitle("Shopware: Select App")
            .setItemChoosenCallback {
                CommandProcessor.getInstance().executeCommand(project, {
                    this.createFile(jbAppList.selectedValue, scriptHook, project)

                }, "Create Hook", null)
            }
            .createPopup()
            .showInFocusCenter()
    }

    private fun createFile(app: ShopwareApp, scriptHook: ScriptHook, project: Project) {
        val localFolder = LocalFileSystem.getInstance().findFileByPath(app.rootFolder)
        val psiDir = PsiManager.getInstance(project).findDirectory(localFolder!!) as PsiDirectory

        val scriptsDir = PsiFolderUtil.createFolderRecursive(psiDir, "Resources/scripts/${scriptHook.name}")

        ActionUtil.createFile(
            project,
            TwigFileType.INSTANCE,
            "script.twig",
            ShopwareTemplates.renderTemplate(
                project,
                ShopwareTemplates.SHOPWARE_APP_HOOK,
                mapOf(
                    "HOOK_NAME" to scriptHook.name,
                    "HOOK_FQN" to scriptHook.fqn,
                    "HOOK_PAGE" to scriptHook.page
                )
            ),
            scriptsDir
        ) ?: return
    }
}