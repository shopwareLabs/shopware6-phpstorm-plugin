package de.shyim.shopware6.intentions

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.*
import com.intellij.ui.components.JBList
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.util.ShopwareBundleUtil
import de.shyim.shopware6.util.TwigUtil
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList


class ExtendTwigBlockIntention : PsiElementBaseIntentionAction() {
    override fun getFamilyName() = "Extends this block in other template directory"
    override fun getText() = "Extends this block in other template directory"


    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return (element is TwigBlockTag || element.parent is TwigBlockTag) && editor != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null || editor !is EditorImpl) {
            return
        }

        val templatePath = TwigUtil.getTemplatePathByFilePath(editor.virtualFile.path, project)
        val currentBundle = TwigUtil.getBundleByFilePath(editor.virtualFile.path, project)

        if (templatePath == null) {
            HintManager.getInstance().showErrorHint(editor, "Cannot determine view folder of currently opened file");
            return
        }

        val bundleList = ShopwareBundleUtil.getAllBundlesRelatedToViews(project).filter { bundle ->
            return@filter bundle.name != currentBundle
        }.filter {
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
            .setTitle("Shopware: Select bundle")
            .setItemChoosenCallback {
                CommandProcessor.getInstance().executeCommand(project, {
                    ApplicationManager.getApplication().runWriteAction {
                        this.extendBlockInBundle(
                            jbBundleList.selectedValue!!,
                            templatePath,
                            getBlockName(element),
                            project
                        )
                    }
                }, "Extend Twig block", null)
            }
            .createPopup()
            .showInBestPositionFor(editor)
    }

    private fun getBlockName(element: PsiElement): String {
        return if (element is TwigBlockTag) {
            element.name!!
        } else {
            (element.parent as TwigBlockTag).name!!
        }
    }

    private fun extendBlockInBundle(bundle: ShopwareBundle, templatePath: String, blockName: String, project: Project) {
        val localFolder = LocalFileSystem.getInstance().findFileByPath(bundle.viewPath)

        var currentFolder: PsiDirectory?
        if (localFolder == null) {
            currentFolder = createMissingViewFolder(bundle, project)
        } else {
            currentFolder = PsiManager.getInstance(project).findDirectory(localFolder) as PsiDirectory
        }

        var fileName: String? = null

        val templateParts = templatePath.split("/")

        templateParts.forEach { part ->
            if (!part.endsWith(".twig")) {
                currentFolder = if (currentFolder!!.findSubdirectory(part) != null) {
                    currentFolder!!.findSubdirectory(part)!!
                } else {
                    currentFolder!!.createSubdirectory(part)
                }
            } else {
                fileName = part
            }
        }

        val blockCode = """
{% block ${blockName} %}

{% endblock %}
        """.trimIndent()

        val blockHeader = """
{% sw_extends "@Storefront/storefront/${templatePath}" %}
        """.trimIndent()

        if (currentFolder!!.findFile(fileName!!) != null) {
            val file = currentFolder!!.findFile(fileName!!)!!

            val editor = FileEditorManager.getInstance(project)
                .openTextEditor(OpenFileDescriptor(project, file.containingFile.virtualFile), true) ?: return

            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.document)
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)

            editor.document.insertString(editor.document.textLength, "\n" + blockCode)
            PsiDocumentManager.getInstance(project).commitDocument(editor.document)
        } else {
            val factory = PsiFileFactory.getInstance(project)
            val file = factory.createFileFromText(fileName!!, TwigFileType.INSTANCE, blockHeader + "\n\n" + blockCode)
            currentFolder!!.add(file)

            FileEditorManager.getInstance(project)
                .openTextEditor(OpenFileDescriptor(project, currentFolder!!.findFile(fileName!!)!!.virtualFile), true)
                ?: return
        }
    }

    private fun createMissingViewFolder(bundle: ShopwareBundle, project: Project): PsiDirectory {
        val bundleFile = LocalFileSystem.getInstance().findFileByPath(bundle.path)!!
        val bundleFolder = PsiManager.getInstance(project).findFile(bundleFile)!!.containingDirectory

        if (bundleFolder.findSubdirectory("Resources") == null) {
            bundleFolder.createSubdirectory("Resources")
        }

        val resourcesFolder = bundleFolder.findSubdirectory("Resources")!!

        if (resourcesFolder.findSubdirectory("views") == null) {
            resourcesFolder.createSubdirectory("views")
        }

        val viewFolder = resourcesFolder.findSubdirectory("views")!!

        if (viewFolder.findSubdirectory("storefront") != null) {
            return viewFolder.findSubdirectory("storefront")!!
        }

        return viewFolder.createSubdirectory("storefront")
    }

    override fun checkFile(file: PsiFile?): Boolean {
        return true
    }
}