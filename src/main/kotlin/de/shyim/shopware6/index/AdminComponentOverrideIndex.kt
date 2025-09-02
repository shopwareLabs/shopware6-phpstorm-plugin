package de.shyim.shopware6.index

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.AdminComponentOverride
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import de.shyim.shopware6.util.StringUtil

class AdminComponentOverrideIndex : FileBasedIndexExtension<String, AdminComponentOverride>() {
    private val _externalizer = ObjectStreamDataExternalizer<AdminComponentOverride>()

    override fun getName(): ID<String, AdminComponentOverride> {
        return key
    }

    override fun getVersion(): Int {
        return 2
    }

    override fun getIndexer(): DataIndexer<String, AdminComponentOverride, FileContent> {
        return DataIndexer { inputData ->
            val components = HashMap<String, AdminComponentOverride>()

            val stringLiteral = PlatformPatterns.psiElement(JSTokenTypes.STRING_LITERAL)

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is JSCallExpression) {
                        if (element.methodExpression == null || element.argumentList == null || element.argumentList!!.arguments.size < 2) {
                            return
                        }

                        if (element.methodExpression!!.firstChild is JSReferenceExpression && element.methodExpression!!.firstChild.firstChild is LeafPsiElement && element.methodExpression!!.firstChild.firstChild.text == "Component") {
                            if (element.methodExpression!!.lastChild is LeafPsiElement && element.methodExpression!!.lastChild.text == "override") {
                                val arguments = element.argumentList!!.arguments
                                val componentName = arguments[0].firstChild

                                if (!stringLiteral.accepts(componentName)) {
                                    return
                                }

                                val component = AdminComponentOverride(
                                    StringUtil.stripQuotes(componentName.text),
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

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<AdminComponentOverride> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, AdminComponentOverride>("de.shyim.shopware6.admin.component_override")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JavaScriptFileType) {
        }
    }
}