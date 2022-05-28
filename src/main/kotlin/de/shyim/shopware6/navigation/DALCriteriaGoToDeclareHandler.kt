package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import de.shyim.shopware6.completion.DALCompletionProvider
import de.shyim.shopware6.index.dict.EntityDefinition
import de.shyim.shopware6.util.EntityDefinitionUtil
import de.shyim.shopware6.util.PHPPattern
import de.shyim.shopware6.util.StringUtil

class DALCriteriaGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (editor === null || editor.project === null || element == null) {
            return null
        }

        val project = editor.project!!

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (PHPPattern.isCriteriaPatternAddAssociation().accepts(element) && PHPPattern.isShopwareCriteriaAddFields(
                element
            )
        ) {
            val definition = DALCompletionProvider.findDefinitionOfCriteria(element)
            if (definition != null) {
                val text = StringUtil.stripQuotes(element.parent.text)
                val referencedDefinition = EntityDefinitionUtil.getDefinitionByInput(project, definition, text)

                addPsiReference(referencedDefinition, project, psiElements)
            }
        } else if (
            ((PHPPattern.isCriteriaPatternAddAssociations().accepts(element) || PHPPattern.isCriteriaPatternAddFilter()
                .accepts(element)) && PHPPattern.isShopwareCriteriaAddFields(element.parent.parent)) ||
            PHPPattern.isCriteriaPatternAddAggregation()
                .accepts(element) && PHPPattern.isShopwareCriteriaAddAggregation(element.parent.parent)
        ) {
            val definition = DALCompletionProvider.findDefinitionOfCriteria(element.parent.parent)
            if (definition != null) {
                val text = StringUtil.stripQuotes(element.parent.text)
                val referencedDefinition = EntityDefinitionUtil.getDefinitionByInput(project, definition, text)

                addPsiReference(referencedDefinition, project, psiElements)
            }
        }

        return psiElements.toTypedArray()
    }

    private fun addPsiReference(
        referencedDefinition: EntityDefinition?,
        project: Project,
        psiElements: MutableList<PsiElement>
    ) {
        if (referencedDefinition != null) {
            val file = LocalFileSystem.getInstance().findFileByPath(referencedDefinition.file)

            if (file != null) {
                val psi = PsiManager.getInstance(project).findFile(file)
                if (psi != null) {
                    psiElements.add(psi)
                }
            }
        }
    }
}