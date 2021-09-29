package de.shyim.shopware6.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.ClassConstantReference
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.PhpReturn
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl
import com.jetbrains.php.lang.psi.elements.impl.ClassConstantReferenceImpl
import de.shyim.shopware6.index.dict.EntityDefinition
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap

class EntityDefinitionIndex : FileBasedIndexExtension<String, EntityDefinition>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<EntityDefinition>()

    override fun getName(): ID<String, EntityDefinition> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, EntityDefinition, FileContent> {
        return DataIndexer { inputData ->
            val entities = THashMap<String, EntityDefinition>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(phpClass: PsiElement) {
                    if (phpClass is PhpClass && !phpClass.isAbstract && isShopwareDefinition(phpClass)) {
                        var name = ""

                        phpClass.findMethodByName("getEntityName")!!
                            .acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                override fun visitElement(element: PsiElement) {
                                    if (element is PhpReturn) {
                                        if (element.lastChild.prevSibling is StringLiteralExpression) { // Easy mode someone hardcoded the entity name in return
                                            name = element.lastChild.prevSibling.text.replace("'", "").replace("\"", "")
                                        } else if (element.lastChild.prevSibling is ClassConstantReference) {
                                            val reference = element.lastChild.prevSibling as ClassConstantReferenceImpl

                                            val resolvedMembers = reference.resolveMember(phpClass, true)
                                            if (resolvedMembers.size > 0) {
                                                name =
                                                    (resolvedMembers.first() as ClassConstImpl).defaultValue!!.text!!.replace(
                                                        "'",
                                                        ""
                                                    ).replace("\"", "")
                                            }
                                        }
                                    }

                                    super.visitElement(element)
                                }
                            })

                        if (name.isNotEmpty()) {
                            entities[name] = EntityDefinition(name, inputData.file.path)
                        }
                    }

                    super.visitElement(phpClass)
                }
            })

            return@DataIndexer entities
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<EntityDefinition> {
        return EXTERNALIZER
    }

    companion object {
        val key = ID.create<String, EntityDefinition>("de.shyim.shopware6.backend.entity-definition")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(PhpFileType.INSTANCE) {
        }
    }

    fun isShopwareDefinition(pClass: PhpClass): Boolean {
        if (pClass.extendsList.referenceElements.size == 0) {
            return false
        }

        var found = false

        pClass.extendsList.referenceElements.forEach {
            if (it.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\EntityDefinition" ||
                it.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\MappingEntityDefinition" ||
                it.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\EntityTranslationDefinition"
            ) {
                found = true
            }
        }

        return found
    }
}