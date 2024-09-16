package de.shyim.shopware6.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.*
import com.intellij.ui.components.JBList
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.roots.PhpNamespaceByFilesProvider
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.util.ShopwareBundleUtil
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

class CreateEventListenerIntention : PsiElementBaseIntentionAction() {
    override fun getFamilyName() = "Subscribe to this event using a event listener"
    override fun getText() = "Subscribe to this event using a event listener"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (element.parent !is PhpClass || editor == null) {
            return false
        }

        val phpClass = element.parent as PhpClass
        var currentClass = phpClass

        while (true) {
            if (currentClass.superFQN === null || currentClass.supers.isEmpty()) {
                return false
            }

            if (currentClass.superFQN == "\\Symfony\\Contracts\\EventDispatcher\\Event") {
                return true
            } else {
                currentClass = currentClass.supers[0]
            }
        }
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val phpClass = element.parent as PhpClass

        val bundleList = ShopwareBundleUtil.getAllBundles(project).filter {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(it.path)!!
            val psiFile = PsiManager.getInstance(project).findFile(virtualFile)!!

            return@filter psiFile.manager.isInProject(psiFile)
        }.sortedBy { shopwareBundle -> shopwareBundle.name }

        val jbBundleList = JBList(bundleList)

        jbBundleList.cellRenderer = object : JBList.StripedListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                if (renderer is JLabel && value is ShopwareBundle) {
                    renderer.text = value.name
                }

                return renderer
            }
        }

        PopupChooserBuilder(jbBundleList)
            .setTitle("Shopware: Select Bundle")
            .setItemChoosenCallback {
                CommandProcessor.getInstance().executeCommand(project, {
                    ApplicationManager.getApplication().runWriteAction {
                        this.createEventListener(
                            jbBundleList.selectedValue!!,
                            phpClass.fqn,
                            project
                        )
                    }
                }, "Create Event Listener", null)
            }
            .createPopup()
            .showInBestPositionFor(editor!!)
    }

    private fun createEventListener(bundle: ShopwareBundle, eventClassName: String, project: Project) {
        val classParts = eventClassName.split("\\")
        val eventShort = classParts[classParts.size - 1]
        val className = "${eventShort}Listener"
        val classFileName = "${eventShort}Listener.php"

        val bundleFile = LocalFileSystem.getInstance().findFileByPath(bundle.path)!!
        val bundleFolder = PsiManager.getInstance(project).findFile(bundleFile)!!.containingDirectory
        var expectedFolder = bundleFolder.findSubdirectory("EventListener")

        if (expectedFolder == null) {
            expectedFolder = bundleFolder.createSubdirectory("EventListener")
        }

        if (expectedFolder.findFile(classFileName) != null) {
            Messages.showInfoMessage("File exists", "Error")
            return
        }

        val content = ShopwareTemplates.renderTemplate(
            project,
            ShopwareTemplates.SHOPWARE_PHP_EVENT_LISTENER,
            mapOf(
                "NAMESPACE" to getNamespaceOfFolder(expectedFolder).replace("\\\\", "\\"),
                "EVENT" to eventClassName.substring(1),
                "EVENT_SHORT" to eventShort,
                "CLASSNAME" to className,
            )
        )

        val factory = PsiFileFactory.getInstance(project)
        val file = factory.createFileFromText(classFileName, PhpFileType.INSTANCE, content)
        expectedFolder.add(file)

        FileEditorManager.getInstance(project)
            .openTextEditor(OpenFileDescriptor(project, expectedFolder.findFile(classFileName)!!.virtualFile), true)
            ?: return
    }

    private fun getNamespaceOfFolder(folder: PsiDirectory): String {
        val namespaces = PhpNamespaceByFilesProvider.INSTANCE.suggestNamespaces(folder)

        if (namespaces.isNotEmpty()) {
            return namespaces[0]
        }

        val superNamespaces = PhpNamespaceByFilesProvider.INSTANCE.suggestNamespaces(folder.parent!!)

        if (superNamespaces.isNotEmpty()) {
            return superNamespaces[0] + "\\\\" + folder.name
        }

        return ""
    }

    override fun checkFile(file: PsiFile?): Boolean {
        return true
    }
}