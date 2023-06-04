package de.shyim.shopware6.index

import com.intellij.lang.ecmascript6.psi.ES6ImportDeclaration
import com.intellij.lang.ecmascript6.psi.ES6Property
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.lang.javascript.psi.JSProperty
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.AdminComponent
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import de.shyim.shopware6.util.StringUtil

class AdminComponentIndex : FileBasedIndexExtension<String, AdminComponent>() {
    private val _externalizer = ObjectStreamDataExternalizer<AdminComponent>()

    override fun getName(): ID<String, AdminComponent> {
        return key
    }

    override fun getVersion(): Int {
        return 3
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, AdminComponent, FileContent> {
        return DataIndexer { inputData ->
            val components = HashMap<String, AdminComponent>()

            val stringLiteral = PlatformPatterns.psiElement(JSTokenTypes.STRING_LITERAL)

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
                                var templatePath: String? = null

                                val componentName = arguments[0].firstChild

                                if (!stringLiteral.accepts(componentName)) {
                                    return
                                }

                                if (element.methodExpression!!.lastChild.text == "extend" && arguments.size >= 2) {
                                    val extendsName = arguments[1].firstChild

                                    if (stringLiteral.accepts(extendsName)) {
                                        extendsFrom = extendsName.text.replace("'", "").replace("\"", "")
                                    }
                                }

                                val propsSet = HashSet<String>()

                                var visitObject: JSObjectLiteralExpression? = null

                                if (element.methodExpression!!.lastChild.text == "register" && element.argumentList!!.arguments.size >= 2 && element.argumentList!!.arguments[1] is JSObjectLiteralExpression) {
                                    visitObject = element.argumentList!!.arguments[1] as JSObjectLiteralExpression
                                } else if (element.methodExpression!!.lastChild.text == "extend" && element.argumentList!!.arguments.size >= 3 && element.argumentList!!.arguments[2] is JSObjectLiteralExpression) {
                                    visitObject = element.argumentList!!.arguments[2] as JSObjectLiteralExpression
                                }

                                if (visitObject != null) {
                                    val props = visitObject.findProperty("props")
                                    val template = visitObject.findProperty("template")

                                    if (props is JSProperty && props.value is JSObjectLiteralExpression) {
                                        val properties = (props.value as JSObjectLiteralExpression).properties

                                        properties.forEach {
                                            if (it.name != null) {
                                                propsSet.add(it.name!!)
                                            }
                                        }
                                    }

                                    if (template is ES6Property) {
                                        inputData.psiFile.children.forEach {
                                            if (it is ES6ImportDeclaration && it.importedBindings.size == 1 && it.importedBindings[0].name == "template" && it.fromClause?.referenceText != null) {
                                                templatePath = StringUtil.stripQuotes(it.fromClause!!.referenceText.toString())

                                                if (templatePath!!.startsWith("./")) {
                                                    val path = templatePath!!.substring(2)
                                                    templatePath = "${inputData.file.parent.path}/${path}"
                                                }
                                            }
                                        }
                                    }
                                }

                                val component = AdminComponent(
                                    StringUtil.stripQuotes(componentName.text),
                                    extendsFrom,
                                    templatePath,
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
        return _externalizer
    }

    companion object {
        val key = ID.create<String, AdminComponent>("de.shyim.shopware6.admin.component")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JavaScriptFileType.INSTANCE) {
        }
    }
}