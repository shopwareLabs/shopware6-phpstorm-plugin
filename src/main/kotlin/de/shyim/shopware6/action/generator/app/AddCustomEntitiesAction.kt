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
import icons.ShopwareToolBoxIcons
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

class AddCustomEntitiesAction: DumbAwareAction("Add Custom Entities", "Add custom entities to an app", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        // Let the user choose the app
        this.chooseApp(e.project!!)
    }

    private fun chooseApp(project: Project) {
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
                        var resourcesDirectory = psiDirectory.findSubdirectory("Resources")

                        if (resourcesDirectory == null) {
                            resourcesDirectory = psiDirectory.createSubdirectory("Resources")
                        }

                        // Create entities.xml
                        val content = ShopwareTemplates.renderTemplate(
                            project,
                            ShopwareTemplates.SHOPWARE_APP_CUSTOM_ENTITIES,
                            null
                        )

                        val customEntitiesDefinition = ActionUtil.createFile(
                            project,
                            XmlFileType.INSTANCE,
                            "entities.xml",
                            content,
                            resourcesDirectory
                        ) ?: return@executeCommand

                        FileEditorManager.getInstance(project)
                            .openTextEditor(OpenFileDescriptor(project, customEntitiesDefinition.virtualFile), true)
                    }, "Create Custom Entities", null)
                }
                .createPopup()
                .showInFocusCenter()
    }
}