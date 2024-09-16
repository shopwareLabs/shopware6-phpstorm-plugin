package de.shyim.shopware6.action.generator.app

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBList
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.index.dict.ShopwareApp
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.util.ShopwareAppUtil
import java.awt.Component
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JList

abstract class AddConfigFileAction(
    private val configFileName: String,
    private val configFilePath: String,
    private val shopwareTemplate: String,
    text: String,
    description: String,
    icon: Icon
) : DumbAwareAction(text, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        // Let the user choose the app
        this.chooseAppAndCreateConfigFileFromTemplate(
            e.project!!,
            this.configFileName,
            this.configFilePath,
            this.shopwareTemplate
        )
    }

    private fun chooseAppAndCreateConfigFileFromTemplate(
        project: Project,
        configFile: String,
        configFilePath: String,
        shopwareTemplate: String
    ) {
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
                    val localFolder = LocalFileSystem.getInstance().findFileByPath(jbAppList.selectedValue.rootFolder)
                    val psiDirectory = PsiManager.getInstance(project).findDirectory(localFolder!!) as PsiDirectory
                    var configFileDirectory = psiDirectory.findSubdirectory(configFilePath)

                    if (configFileDirectory == null) {
                        configFileDirectory = psiDirectory.createSubdirectory(configFilePath)
                    }

                    // Create a config file from template
                    val content = ShopwareTemplates.renderTemplate(
                        project,
                        shopwareTemplate,
                        null
                    )

                    val createdConfigFile = ActionUtil.createFile(
                        project,
                        XmlFileType.INSTANCE,
                        configFile,
                        content,
                        configFileDirectory
                    ) ?: return@executeCommand

                    FileEditorManager.getInstance(project)
                        .openTextEditor(OpenFileDescriptor(project, createdConfigFile.virtualFile), true)
                }, "Creating Config File", null)
            }
            .createPopup()
            .showInFocusCenter()
    }
}