package de.shyim.shopware6.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.*
import com.jetbrains.php.lang.psi.elements.impl.ClassConstImpl
import com.jetbrains.php.lang.psi.elements.impl.ClassConstantReferenceImpl
import de.shyim.shopware6.index.dict.EntityDefinition
import de.shyim.shopware6.index.dict.EntityDefinitionField
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import de.shyim.shopware6.util.StringUtil
import gnu.trove.THashMap

class EntityDefinitionIndex : FileBasedIndexExtension<String, EntityDefinition>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<EntityDefinition>()

    override fun getName(): ID<String, EntityDefinition> {
        return key
    }

    override fun getVersion(): Int {
        return 2
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
                        val fields = mutableListOf<EntityDefinitionField>()

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

                        phpClass.findMethodByName("defineFields")
                            ?.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                override fun visitElement(element: PsiElement) {
                                    if (element is NewExpression) {
                                        getFieldByExpression(element)?.let { fields.add(it) }
                                    }

                                    super.visitElement(element)
                                }
                            })

                        if (name.isNotEmpty()) {
                            entities[phpClass.fqn] = EntityDefinition(name, phpClass.fqn, inputData.file.path, fields)
                        }
                    }

                    super.visitElement(phpClass)
                }
            })

            return@DataIndexer entities
        }
    }

    private fun getFieldByExpression(element: NewExpression): EntityDefinitionField? {
        if (
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\BlobField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\BoolField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\CalculatedPriceField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\CartPriceField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\CashRoundingConfigField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ChildrenAssociationField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ConfigJsonField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\DateField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\DateTimeField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\EmailField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\FkField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\FloatField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\IdField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\IntField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\JsonField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ListField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\LongTextField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ObjectField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\PasswordField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\PriceDefinitionField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\PriceField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\RemoteAddressField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\StateMachineStateField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\StringField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\TaxFreeConfigField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\TimeZoneField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\TreeLevelField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ManyToManyIdField" ||
            element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\TreePathField"
        ) {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(1)?.text.toString()),
                element.classReference!!.fqn!!,
                false
            )
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\CreatedByField") {
            return EntityDefinitionField("createdById", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\UpdatedByField") {
            return EntityDefinitionField("updatedById", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\CustomFields") {
            return EntityDefinitionField("customFields", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\LockedField") {
            return EntityDefinitionField("locked", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ParentAssociationField") {
            return EntityDefinitionField("parent", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ParentFkField") {
            return EntityDefinitionField("parentId", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\VersionField") {
            return EntityDefinitionField("versionId", element.classReference!!.fqn!!, false)
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\TranslatedField") {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(0)?.text.toString()),
                element.classReference!!.fqn!!,
                false
            )
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\TranslatedField") {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(0)?.text.toString()),
                element.classReference!!.fqn!!,
                false
            )
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ManyToOneAssociationField" && element.getParameter(
                2
            ) is ClassConstantReference
        ) {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(0)?.text.toString()),
                element.classReference!!.fqn!!,
                true,
                (element.getParameter(2) as ClassConstantReference).classReference?.type.toString()
            )
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\OneToManyAssociationField" && element.getParameter(
                1
            ) is ClassConstantReference
        ) {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(0)?.text.toString()),
                element.classReference!!.fqn!!,
                true,
                (element.getParameter(1) as ClassConstantReference).classReference?.type.toString()
            )
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ManyToManyAssociationField" && element.getParameter(
                1
            ) is ClassConstantReference
        ) {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(0)?.text.toString()),
                element.classReference!!.fqn!!,
                true,
                (element.getParameter(1) as ClassConstantReference).classReference?.type.toString()
            )
        } else if (element.classReference?.fqn == "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\OneToOneAssociationField" && element.getParameter(
                3
            ) is ClassConstantReference
        ) {
            return EntityDefinitionField(
                StringUtil.stripQuotes(element.getParameter(0)?.text.toString()),
                element.classReference!!.fqn!!,
                true,
                (element.getParameter(3) as ClassConstantReference).classReference?.type.toString()
            )
        }

        return null
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