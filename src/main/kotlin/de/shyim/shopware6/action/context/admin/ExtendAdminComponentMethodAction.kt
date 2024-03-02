package de.shyim.shopware6.action.context.admin

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.impl.JSPropertyImpl
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ui.components.JBList
import com.intellij.util.containers.filterSmartMutable
import de.shyim.shopware6.index.dict.AdminComponent
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.util.ShopwareBundleUtil
import de.shyim.shopware6.util.StringUtil
import icons.ShopwareToolBoxIcons
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

class ExtendAdminComponentMethodAction: DumbAwareAction("Extend this method", "Extend this method in your component", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        val pf: PsiFile = LangDataKeys.PSI_FILE.getData(e.dataContext) ?: return
        val editor = LangDataKeys.EDITOR.getData(e.dataContext) ?: return
        val pe = pf.findElementAt(editor.caretModel.offset) ?: return

        extendMethod(pe, editor)
    }

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

        if (!getPattern().accepts(element)) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
    }

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    companion object {
        private const val newComponent = "Create new component"

        fun extendMethod(element: PsiElement, editor: Editor) {
            val componentName = getComponentName(element)

            val popup = ShopwareBundleUtil.getBundleSelectionPopup(element.project)
            popup
                .setItemChoosenCallback {
                    val bundle =
                        (popup.chooserComponent as JBList<ShopwareBundle>).selectedValue ?: return@setItemChoosenCallback

                    val allExistingComponents = ShopwareBundleUtil.getAllComponentsWithOverridesInBundle(bundle, element.project).filterSmartMutable { component ->
                        return@filterSmartMutable component.extends == componentName || (component.extends == componentName && component.templatePath == "override")
                    }

                    allExistingComponents.add(AdminComponent(newComponent, null, null, HashSet(), ""))

                    val list = JList(allExistingComponents.toTypedArray())
                    list.cellRenderer = object : JBList.StripedListCellRenderer() {
                        override fun getListCellRendererComponent(
                            list: JList<*>?,
                            value: Any?,
                            index: Int,
                            isSelected: Boolean,
                            cellHasFocus: Boolean
                        ): Component {
                            val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                            if (renderer is JLabel && value is AdminComponent) {
                                renderer.text = value.name
                            }

                            return renderer
                        }
                    }
                    PopupChooserBuilder(list)
                        .setFilteringEnabled {
                            return@setFilteringEnabled (it as AdminComponent).name
                        }
                        .setItemChoosenCallback {
                            val component = list.selectedValue ?: return@setItemChoosenCallback

                            if (component.name == newComponent) {
                                ExtendAdminComponentAction.createComponent(componentName, element.project, editor, bundle) {
                                    addMethodToComponent(element, it)
                                }
                            } else {
                                addMethodToComponent(element, component.file)
                            }
                        }
                        .createPopup()
                        .showInBestPositionFor(editor)
                }
                .createPopup()
                .showInBestPositionFor(editor)
        }

        private fun addMethodToComponent(baseElement: PsiElement, path: String) {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(path)!!

            FileEditorManager.getInstance(baseElement.project).openTextEditor(OpenFileDescriptor(baseElement.project, virtualFile), true)

            val psiFile = PsiManager.getInstance(baseElement.project).findFile(virtualFile)!!

            val methodName = baseElement.text
            val propertyName = if (baseElement.parent.parent.parent is JSArgumentList) {
                ""
            } else {
                (baseElement.parent.parent.parent as JSPropertyImpl).name!!
            }

            var methodHead = ""
            var methodPass = ""

            (baseElement.parent as JSFunctionProperty).parameters.forEach {
                methodHead = if (methodHead.isEmpty()) {
                    it.name!!
                } else {
                    "${methodHead}, ${it.name}"
                }
            }

            if (methodHead.isNotEmpty()) {
                methodPass = ", $methodHead"
            }



            psiFile.acceptChildren(object: PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (getBasePattern().accepts(element) && element is JSObjectLiteralExpression) {
                        ApplicationManager.getApplication().runWriteAction {
                            CommandProcessor.getInstance().executeCommand(element.project, {
                                val newMethod = PsiFileFactory.getInstance(element.project)
                                    .createFileFromText("test.js", JavascriptLanguage.INSTANCE, "{\n    ${methodName}(${methodHead}) {\n        return this.\$super('${methodName}'${methodPass})    \n    },}\n}")
                                    .firstChild

                                if (propertyName.isNotEmpty()) {
                                    if (element.findProperty(propertyName) != null) {
                                        val existingProperty = element.findProperty(propertyName)!!

                                        val propertyValue = existingProperty.value!!
                                        propertyValue.addBefore(newMethod.children[1], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[2], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[3], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[4], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[6], propertyValue.lastChild)
                                    } else {
                                        val newProperty = PsiFileFactory.getInstance(element.project)
                                            .createFileFromText("test.js", JavascriptLanguage.INSTANCE, "{${propertyName}: {}}")
                                            .firstChild.children[1]

                                        element.addBefore(newProperty, element.lastChild)

                                        val propertyValue = element.children[element.children.size - 1].lastChild

                                        propertyValue.addBefore(newMethod.children[1], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[2], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[3], propertyValue.lastChild)
                                        propertyValue.addBefore(newMethod.children[4], propertyValue.lastChild)
                                    }
                                } else {
                                    element.addBefore(newMethod.children[1], element.lastChild)
                                    element.addBefore(newMethod.children[2], element.lastChild)
                                    element.addBefore(newMethod.children[3], element.lastChild)
                                    element.addBefore(newMethod.children[4], element.lastChild)
                                    element.addBefore(newMethod.children[6], element.lastChild)
                                }

                                CodeStyleManager.getInstance(element.project).reformat(element)
                            }, "Adding method", null)
                        }
                        return
                    }

                    super.visitElement(element)
                }
            })
        }

        public fun getPattern(): PsiElementPattern.Capture<PsiElement> {
            return PlatformPatterns
                .psiElement(JSTokenTypes.IDENTIFIER)
                .withParent(
                    PlatformPatterns.psiElement(JSFunctionProperty::class.java)
                        .withParent(
                            PlatformPatterns.or(
                                getOuterPattern(),
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(
                                        PlatformPatterns.psiElement(JSProperty::class.java)
                                            .withParent(getOuterPattern())
                                    )
                            )
                        )
                )
        }

        private fun getOuterPattern(): PsiElementPattern.Capture<JSObjectLiteralExpression> {
            return PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                .withParent(
                    PlatformPatterns.psiElement(JSArgumentList::class.java)
                        .withParent(
                            PlatformPatterns.psiElement(JSCallExpression::class.java)
                                .withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(
                                            PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                .withFirstChild(
                                                    PlatformPatterns.psiElement().withText("Component")
                                                )
                                        )
                                        .withLastChild(PlatformPatterns.or(PlatformPatterns.psiElement().withText("register"), PlatformPatterns.psiElement().withText("extend")))
                                )
                        )
                )
        }

        private fun getBasePattern(): PsiElementPattern.Capture<JSObjectLiteralExpression> {
            return PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                .withParent(
                    PlatformPatterns.psiElement(JSArgumentList::class.java)
                        .withParent(
                            PlatformPatterns.psiElement(JSCallExpression::class.java)
                                .withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(
                                            PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                .withFirstChild(
                                                    PlatformPatterns.psiElement().withText("Component")
                                                )
                                        )
                                        .withLastChild(PlatformPatterns.or(PlatformPatterns.psiElement().withText("register"), PlatformPatterns.psiElement().withText("extend"), PlatformPatterns.psiElement().withText("override")))
                                )
                        )
                )
        }

        private fun getComponentName(element: PsiElement): String {
            var element = element

            while (element !is JSArgumentList) {
                element = element.parent
            }

            return StringUtil.stripQuotes(element.arguments[0].text)
        }
    }
}