package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.EntityDefinitionIndex
import de.shyim.shopware6.index.dict.EntityDefinition
import icons.ShopwareToolBoxIcons

object EntityDefinitionUtil {
    fun getAllDefinitions(project: Project): MutableList<EntityDefinition> {
        val definitions: MutableList<EntityDefinition> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(EntityDefinitionIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(EntityDefinitionIndex.key, key, GlobalSearchScope.allScope(project))

            definitions.addAll(vals)
        }

        return definitions
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllDefinitions(project).forEach {
            list.add(
                LookupElementBuilder.create(it.name)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }

    fun findByFqn(fqn: String, project: Project): EntityDefinition? {
        return FileBasedIndex.getInstance()
            .getValues(EntityDefinitionIndex.key, fqn, GlobalSearchScope.allScope(project)).firstOrNull()
    }

    fun getAllEntityAssociations(
        project: Project,
        criteriaDefinition: String,
        userInput: String
    ): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()
        val parts = userInput.split(".") as MutableList

        val definition = findByFqn(criteriaDefinition, project) ?: return list
        val definitionFields = definition.getAllAssociations()
        val foundAssociation = definitionFields.firstOrNull { it.name == parts.first() }
        val fixedUserInput = fixUserInput(userInput)

        if (parts.first().isEmpty() || foundAssociation == null) {
            definitionFields.forEach {
                list.add(
                    LookupElementBuilder.create(it.name)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )
            }
        } else if (parts.size >= 2) {
            parts.removeAt(0)
            var targetAssociation = findByFqn(foundAssociation.associationTarget, project) ?: return list

            parts.forEachIndexed { index, part ->
                if (parts.size == (index + 1)) {
                    targetAssociation.getAllAssociations().forEach {
                        list.add(
                            LookupElementBuilder.create(fixedUserInput + it.name)
                                .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                        )
                    }
                } else {
                    val nextAssociation =
                        targetAssociation.getAllAssociations().firstOrNull { it.name == part } ?: return list
                    targetAssociation = findByFqn(nextAssociation.associationTarget, project) ?: return list
                }
            }
        }

        return list
    }

    fun getAllEntityFields(
        project: Project,
        criteriaDefinition: String,
        userInput: String
    ): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()
        val parts = userInput.split(".") as MutableList
        val fixedUserInput = fixUserInput(userInput)

        val definition = findByFqn(criteriaDefinition, project) ?: return list
        val definitionFields = definition.fields
        val foundAssociation = definitionFields.firstOrNull { it.name == parts.first() }

        if (parts.first().isEmpty() || foundAssociation == null) {
            definitionFields.forEach {
                list.add(
                    LookupElementBuilder.create(it.name)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )
            }
        } else if (parts.size >= 2) {
            parts.removeAt(0)
            var targetAssociation = findByFqn(foundAssociation.associationTarget, project) ?: return list

            parts.forEachIndexed { index, part ->
                if (parts.size == (index + 1)) {
                    targetAssociation.fields.forEach {
                        list.add(
                            LookupElementBuilder.create(fixedUserInput + it.name)
                                .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                        )
                    }
                } else {
                    val nextAssociation =
                        targetAssociation.fields.firstOrNull { it.name == part } ?: return list
                    targetAssociation = findByFqn(nextAssociation.associationTarget, project) ?: return list
                }
            }
        }

        return list
    }

    private fun fixUserInput(input: String): String {
        val fixedUserInputParts = input.split(".") as MutableList

        if (fixedUserInputParts.first() == "") {
            return ""
        }

        fixedUserInputParts.removeLast()

        if (fixedUserInputParts.size == 0) {
            return ""
        }

        return fixedUserInputParts.joinToString(".") + "."
    }

    fun getDefinitionByInput(project: Project, criteriaDefinition: String, userInput: String): EntityDefinition? {
        val parts = userInput.split(".") as MutableList

        var definition = findByFqn(criteriaDefinition, project) ?: return null

        if (parts.first().isEmpty()) {
            return definition
        }

        parts.forEachIndexed { index, part ->
            val nextAssociation = definition.fields.firstOrNull { it.name == part } ?: return definition
            definition = findByFqn(nextAssociation.associationTarget, project) ?: return definition
        }

        return definition
    }
}