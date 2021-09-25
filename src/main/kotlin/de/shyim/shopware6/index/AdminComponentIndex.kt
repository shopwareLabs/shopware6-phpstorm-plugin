package de.shyim.shopware6.index

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.AdminComponent
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap

class AdminComponentIndex : FileBasedIndexExtension<String, AdminComponent>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<AdminComponent>()

    override fun getName(): ID<String, AdminComponent> {
        return AdminComponentIndex.key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, AdminComponent, FileContent> {
        return DataIndexer { inputData ->
            val components = THashMap<String, AdminComponent>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is JSCallExpression) {
                        if (element.methodExpression == null || element.argumentList == null || element.argumentList!!.arguments.size < 2) {
                            return
                        }

                        if (element.methodExpression!!.firstChild is JSReferenceExpression && element.methodExpression!!.firstChild.firstChild is LeafPsiElement && element.methodExpression!!.firstChild.firstChild.text == "Component") {
                            if (element.methodExpression!!.lastChild is LeafPsiElement && (element.methodExpression!!.lastChild.text == "register" || element.methodExpression!!.lastChild.text == "extend")) {
                                val arguments = element.argumentList!!.arguments
                                var extendsFrom: String? = null

                                val componentName = arguments.get(0).firstChild

                                if (componentName.elementType == null || componentName.elementType!!.debugName != "STRING_LITERAL") {
                                    return
                                }

                                if (element.methodExpression!!.lastChild.text == "extend" && arguments.size >= 2) {
                                    val extendsName = arguments.get(1).firstChild

                                    if (extendsName.elementType != null && extendsName.elementType!!.debugName == "STRING_LITERAL") {
                                        extendsFrom = extendsName.text.replace("'", "").replace("\"", "")
                                    }
                                }

                                val propsSet = HashSet<String>()

                                var visitObject: PsiElement? = null

                                if (element.methodExpression!!.lastChild.text == "register" && element.argumentList!!.arguments.size >= 2) {
                                    visitObject = element.argumentList!!.arguments.get(1)
                                } else if (element.methodExpression!!.lastChild.text == "extend" && element.argumentList!!.arguments.size >= 3) {
                                    visitObject = element.argumentList!!.arguments.get(2)
                                }

                                if (visitObject != null) {
                                    visitObject.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                        override fun visitElement(element: PsiElement) {
                                            if (element is JSProperty) {
                                                if (element.firstChild.text != "props") {
                                                    return
                                                }

                                                element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                                    override fun visitElement(element: PsiElement) {
                                                        if (element is JSObjectLiteralExpression) {
                                                            super.visitElement(element)
                                                            return
                                                        }

                                                        if (element is JSProperty) {
                                                            propsSet.add(element.firstChild.text)
                                                        }
                                                    }
                                                })
                                            }
                                        }
                                    })
                                }

                                val component = AdminComponent(
                                    componentName.text.replace("'", "").replace("\"", ""),
                                    extendsFrom,
                                    propsSet,
                                    inputData.file.path
                                )

                                components[component.name] = component
                            }

                            return
                        }
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer components
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<AdminComponent> {
        return EXTERNALIZER
    }

    companion object {
        val key = ID.create<String, AdminComponent>("de.shyim.shopware6.admin.component")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JavaScriptFileType.INSTANCE) {
        }
    }
}