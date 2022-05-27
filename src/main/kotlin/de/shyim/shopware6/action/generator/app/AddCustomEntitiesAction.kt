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

class AddCustomEntitiesAction: AddConfigFileAction(
        "entities.xml",
        "Resources",
        ShopwareTemplates.SHOPWARE_APP_CUSTOM_ENTITIES,
        "Add Custom Entities",
        "Add custom entities to an app",
        ShopwareToolBoxIcons.SHOPWARE
)