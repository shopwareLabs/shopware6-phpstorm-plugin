package de.shyim.shopware6.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.GroupStatementImpl
import com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl
import de.shyim.shopware6.index.dict.ScriptHook
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap

class ScriptHookIndex : FileBasedIndexExtension<String, ScriptHook>() {
    private val _externalizer = ObjectStreamDataExternalizer<ScriptHook>()

    override fun getName(): ID<String, ScriptHook> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, ScriptHook, FileContent> {
        return DataIndexer { inputData ->
            val hooks = THashMap<String, ScriptHook>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is PhpClass && element.extendsList.referenceElements.isNotEmpty() && isHookClass(element)) {
                        var hookName = ""
                        var hookPage = ""
                        val services: MutableList<String> = arrayListOf()

                        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                            override fun visitElement(element: PsiElement) {
                                if (element is ClassConstImpl && element.name == "HOOK_NAME") {
                                    hookName = (element.defaultValue as StringLiteralExpressionImpl).contents

                                    return
                                }

                                if (element is Method && element.name == "getServiceIds" && element.lastChild is GroupStatementImpl) {
                                    element.lastChild.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                        override fun visitElement(element: PsiElement) {
                                            if (element is ClassReferenceImpl) {
                                                services.add(element.fqn!!)
                                            }

                                            super.visitElement(element)
                                        }
                                    })
                                }

                                if (element is Method && element.name == "getPage") {
                                    element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                        override fun visitElement(element: PsiElement) {
                                            if (element is ClassReferenceImpl) {
                                                hookPage = element.fqn!!
                                            }

                                            super.visitElement(element)
                                        }
                                    })
                                }

                                super.visitElement(element)
                            }
                        })

                        if (hookName.isNotEmpty()) {
                            hooks[hookName] = ScriptHook(
                                hookName,
                                element.fqn,
                                services,
                                hookPage
                            )
                        }

                        return
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer hooks
        }
    }

    fun isHookClass(pClass: PhpClass): Boolean {
        for (reference in pClass.extendsList.referenceElements) {
            if (reference.fqn == "\\Shopware\\Core\\Framework\\Script\\Execution\\Hook" || reference.fqn == "\\Shopware\\Storefront\\Page\\PageLoadedHook") {
                return true
            }
        }

        return false
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<ScriptHook> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, ScriptHook>("de.shyim.shopware6.script.hook")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(PhpFileType.INSTANCE) {
        }
    }
}