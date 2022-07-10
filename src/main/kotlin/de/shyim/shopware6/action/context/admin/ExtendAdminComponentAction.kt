package de.shyim.shopware6.action.context.admin

import com.intellij.codeInsight.hint.HintManager
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.psi.JSFile
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.components.JBList
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.action.generic.GenericSimpleDialogWrapper
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.PsiUtil
import de.shyim.shopware6.util.ShopwareBundleUtil
import de.shyim.shopware6.util.StringUtil
import icons.ShopwareToolBoxIcons
import org.apache.commons.io.FilenameUtils
import java.nio.file.Paths

class ExtendAdminComponentAction : DumbAwareAction(
    "Extend admin component",
    "Extend this admin component in my plugin",
    ShopwareToolBoxIcons.SHOPWARE
) {
    override fun update(e: AnActionEvent) {
        val pf: PsiFile? = LangDataKeys.PSI_FILE.getData(e.dataContext)

        if (pf == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        if (pf !is JSFile) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val editor = LangDataKeys.EDITOR.getData(e.dataContext)

        if (editor == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val element = pf.findElementAt(editor.caretModel.offset)

        if (element == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        if (!JavaScriptPattern.getComponentRegisterFirstParameter().accepts(element)) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        val pf: PsiFile = LangDataKeys.PSI_FILE.getData(e.dataContext) ?: return
        val editor = LangDataKeys.EDITOR.getData(e.dataContext) ?: return
        val pe = pf.findElementAt(editor.caretModel.offset) ?: return

        createComponent(StringUtil.stripQuotes(pe.text), pe.project, editor, null) {}
    }

    companion object {
        fun createComponent(componentName: String, project: Project, editor: Editor, bundle: ShopwareBundle?, runnable: (String) -> Unit) {
            JBPopupFactory
                .getInstance()
                .createConfirmation(
                    "What do you want?",
                    "Override",
                    "Extend",
                    {
                        createComponentCallback("override", componentName, editor, project, runnable, bundle)
                    },
                    {
                        createComponentCallback("extend", componentName, editor, project, runnable, bundle)
                    },
                    0
                )
                .showInBestPositionFor(editor)
        }

        private fun createComponentCallback(type: String, componentName: String, editor: Editor, project: Project, runnable: (String) -> Unit, bundle: ShopwareBundle?) {
            if (bundle != null) {
                ensureAdminEntrypointExists(bundle, project)
                createComponentExtend(type, componentName, editor, bundle, project, runnable)

                return
            }

            val popup = ShopwareBundleUtil.getBundleSelectionPopup(project)
            popup
                .setItemChoosenCallback {
                    val bundle =
                        (popup.chooserComponent as JBList<ShopwareBundle>).selectedValue ?: return@setItemChoosenCallback

                    ensureAdminEntrypointExists(bundle, project)
                    createComponentExtend(type, componentName, editor, bundle, project, runnable)

                }
                .createPopup()
                .showInBestPositionFor(editor)
        }

        private fun createComponentExtend(
            type: String,
            componentName: String,
            editor: Editor,
            bundle: ShopwareBundle,
            project: Project,
            runnable: (String) -> Unit
        ) {
            val newComponentName: String
            val defaultFilePath = if (type == "override") {
                newComponentName = componentName
                "override/${componentName}/index.js"
            } else {
                newComponentName = FilenameUtils.separatorsToUnix((GenericSimpleDialogWrapper("New component name", "Name:", "custom-${componentName}")).showAndGetConfig() ?: return)
                "component/${newComponentName}/index.js"
            }

            val chosenComponentEntry = FilenameUtils.separatorsToUnix((GenericSimpleDialogWrapper("Component path", "Filename:", defaultFilePath)).showAndGetConfig() ?: return)
            val chosenComponentFolder = FilenameUtils.separatorsToUnix(Paths.get(chosenComponentEntry).parent.toString())

            val adminRoot = PsiManager.getInstance(project)
                .findDirectory(LocalFileSystem.getInstance().findFileByPath(bundle.getAdministrationRoot())!!)!!
            val componentFolder = PsiUtil.createFolderRecursive(adminRoot, chosenComponentFolder)

            if (componentFolder.findFile("index.js") != null) {
                HintManager.getInstance().showErrorHint(editor, "The path exists already")
                return
            }

            val templateVars = mapOf("NAME" to newComponentName, "EXTEND" to componentName)

            val content = if (type == "override") {
                ShopwareTemplates.renderTemplate(
                    project,
                    ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT_OVERRIDE,
                    templateVars
                )
            } else {
                ShopwareTemplates.renderTemplate(
                    project,
                    ShopwareTemplates.SHOPWARE_ADMIN_VUE_COMPONENT_EXTEND,
                    templateVars
                )
            }

            val file = ActionUtil.createFile(
                project,
                JavaScriptFileType.INSTANCE,
                "index.js",
                content,
                componentFolder
            )

            if (file != null) {
                runnable(file.virtualFile.path)
            }

            val entryPoint = LocalFileSystem.getInstance().findFileByPath(bundle.getAdministrationEntrypoint())!!

            val entrypointEditor = FileEditorManager.getInstance(project)
                .openTextEditor(OpenFileDescriptor(project, entryPoint), false) ?: return

            ApplicationManager.getApplication().runWriteAction {
                CommandProcessor.getInstance().executeCommand(project, {
                    PsiDocumentManager.getInstance(project)
                        .doPostponedOperationsAndUnblockDocument(entrypointEditor.document)
                    PsiDocumentManager.getInstance(project).commitDocument(entrypointEditor.document)

                    entrypointEditor.document.insertString(
                        entrypointEditor.document.textLength,
                        "\nimport \"./${chosenComponentFolder}\";"
                    )
                    PsiDocumentManager.getInstance(project).commitDocument(entrypointEditor.document)
                }, "Inserting entrypoint", null)
            }
        }

        private fun ensureAdminEntrypointExists(bundle: ShopwareBundle, project: Project) {
            val adminRootVirtual = LocalFileSystem.getInstance().findFileByPath(bundle.getAdministrationRoot())

            val adminRoot: PsiDirectory = if (adminRootVirtual == null) {
                val bundleRoot = LocalFileSystem.getInstance().findFileByPath(bundle.rootFolder)
                val psiFolder = PsiManager.getInstance(project).findDirectory(bundleRoot!!)

                PsiUtil.createFolderRecursive(psiFolder!!, "Resources/app/administration/src")
            } else {
                PsiManager.getInstance(project).findDirectory(adminRootVirtual)!!
            }

            if (adminRoot.findFile("main.js") == null) {
                ActionUtil.createFile(project, JavaScriptFileType.INSTANCE, "main.js", "", adminRoot)
            }
        }
    }
}