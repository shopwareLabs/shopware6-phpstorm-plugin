package de.shyim.shopware6.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpReturnType
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl
import de.shyim.shopware6.index.dict.ScriptHookFacade
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap

class ScriptHookFacadeIndex : FileBasedIndexExtension<String, ScriptHookFacade>() {
    private val _externalizer = ObjectStreamDataExternalizer<ScriptHookFacade>()

    override fun getName(): ID<String, ScriptHookFacade> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, ScriptHookFacade, FileContent> {
        return DataIndexer { inputData ->
            val facades = THashMap<String, ScriptHookFacade>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is PhpClass && element.extendsList.referenceElements.isNotEmpty() && isFacade(element)) {
                        var facadeName = ""
                        var fqn = ""

                        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                            override fun visitElement(element: PsiElement) {
                                if (element is Method && element.name == "getName") {
                                    element.lastChild.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                        override fun visitElement(element: PsiElement) {
                                            if (element is StringLiteralExpressionImpl) {
                                                facadeName = element.contents
                                                return
                                            }

                                            super.visitElement(element)
                                        }
                                    })

                                    return
                                }

                                if (element is Method && element.name == "factory") {
                                    for (child in element.children) {
                                        if (child is PhpReturnType) {
                                            fqn = (child.firstChild as ClassReferenceImpl).fqn!!
                                        }
                                    }
                                }

                                super.visitElement(element)
                            }
                        })

                        if (facadeName.isNotEmpty()) {
                            facades[element.fqn] = ScriptHookFacade(
                                facadeName,
                                fqn,
                            )
                        }

                        return
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer facades
        }
    }

    fun isFacade(pClass: PhpClass): Boolean {
        for (reference in pClass.extendsList.referenceElements) {
            if (reference.fqn == "\\Shopware\\Core\\Framework\\Script\\Execution\\Awareness\\HookServiceFactory") {
                return true
            }
        }

        return false
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<ScriptHookFacade> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, ScriptHookFacade>("de.shyim.shopware6.script.hook_facade")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(PhpFileType.INSTANCE) {
        }
    }
}