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
import de.shyim.shopware6.index.dict.AdminMixin
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer

class AdminMixinIndex : FileBasedIndexExtension<String, AdminMixin>() {
    private val _externalizer = ObjectStreamDataExternalizer<AdminMixin>()

    override fun getName(): ID<String, AdminMixin> {
        return key
    }

    override fun getVersion(): Int {
        return 2
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, AdminMixin, FileContent> {
        return DataIndexer { inputData ->
            val mixins = HashMap<String, AdminMixin>()

            val stringLiteral = PlatformPatterns.psiElement(JSTokenTypes.STRING_LITERAL)

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is JSCallExpression) {
                        if (element.methodExpression == null || element.argumentList == null || element.argumentList!!.arguments.size < 2) {
                            return
                        }

                        if (element.methodExpression!!.firstChild is JSReferenceExpression && element.methodExpression!!.firstChild.firstChild is LeafPsiElement && element.methodExpression!!.firstChild.firstChild.text == "Mixin") {
                            if (element.methodExpression!!.lastChild is LeafPsiElement && element.methodExpression!!.lastChild.text == "register") {
                                val arguments = element.argumentList!!.arguments

                                val mixingName = arguments[0].firstChild

                                if (!stringLiteral.accepts(mixingName)) {
                                    return
                                }

                                val mixin = AdminMixin(
                                    mixingName.text.replace("'", "").replace("\"", ""),
                                    inputData.file.path
                                )

                                mixins[mixin.name] = mixin
                            }

                            return
                        }
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer mixins
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<AdminMixin> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, AdminMixin>("de.shyim.shopware6.admin.mixin")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JavaScriptFileType.INSTANCE) {
        }
    }
}