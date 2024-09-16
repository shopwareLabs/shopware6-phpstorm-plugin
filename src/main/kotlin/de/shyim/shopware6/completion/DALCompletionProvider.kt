package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.*
import de.shyim.shopware6.util.EntityDefinitionUtil
import de.shyim.shopware6.util.PHPPattern
import de.shyim.shopware6.util.StringUtil
import java.util.regex.Pattern

class DALCompletionProvider : CompletionContributor() {
    init {
        // addAssociation
        extend(
            CompletionType.BASIC,
            PHPPattern.isCriteriaPatternAddAssociation(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    if (PHPPattern.isShopwareCriteriaAddFields(element)) {
                        val definition = findDefinitionOfCriteria(element) ?: return
                        result.addAllElements(
                            EntityDefinitionUtil.getAllEntityAssociations(
                                project,
                                definition,
                                StringUtil.stripQuotes(element.text)
                            )
                        )
                    }
                }
            }
        )

        // addAssociations
        extend(
            CompletionType.BASIC,
            PHPPattern.isCriteriaPatternAddAssociations(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    if (PHPPattern.isShopwareCriteriaAddFields(element.parent.parent)) {
                        val definition = findDefinitionOfCriteria(element.parent.parent) ?: return
                        result.addAllElements(
                            EntityDefinitionUtil.getAllEntityAssociations(
                                project,
                                definition,
                                StringUtil.stripQuotes(element.text)
                            )
                        )
                    }
                }
            }
        )

        // addFilter, addPostFilter, addSorting, addGroupField
        extend(
            CompletionType.BASIC,
            PHPPattern.isCriteriaPatternAddFilter(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    if (PHPPattern.isShopwareCriteriaAddFields(element.parent.parent)) {
                        val definition = findDefinitionOfCriteria(element.parent.parent) ?: return
                        result.addAllElements(
                            EntityDefinitionUtil.getAllEntityFields(
                                project,
                                definition,
                                StringUtil.stripQuotes(element.text)
                            )
                        )
                    }
                }
            }
        )

        // addAggregation
        extend(
            CompletionType.BASIC,
            PHPPattern.isCriteriaPatternAddAggregation(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    if (PHPPattern.isShopwareCriteriaAddAggregation(element.parent.parent)) {
                        val definition = findDefinitionOfCriteria(element.parent.parent) ?: return
                        result.addAllElements(
                            EntityDefinitionUtil.getAllEntityFields(
                                project,
                                definition,
                                StringUtil.stripQuotes(element.text)
                            )
                        )
                    }
                }
            }
        )
    }

    companion object {
        private const val REGEX = "<(?<generic>.*)>"

        private val pattern: Pattern = Pattern.compile(REGEX, Pattern.MULTILINE)

        fun findDefinitionOfCriteria(element: PsiElement): String? {
            // Resolve the variable name
            var methodReference = element

            while (methodReference !is MethodReference && methodReference !is PhpFile) {
                methodReference = methodReference.parent
            }

            // The first parameter of the addAssociation has to be a variable; otherwise we cannot know what it is
            if (methodReference.firstChild !is Variable) {
                return null
            }

            val variableName = (methodReference.firstChild as Variable).name


            // Find the closest $criteria variable with a doc block
            var groupStatement = element

            while (groupStatement !is GroupStatement && groupStatement !is Method && groupStatement !is PhpFile) {
                groupStatement = groupStatement.parent
            }

            var foundComment: PhpDocComment? = null

            groupStatement.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is Variable && element.name == variableName && element.docComment != null) {
                        foundComment = element.docComment
                    }

                    super.visitElement(element)
                }
            })

            if (foundComment == null) {
                return null
            }

            val matcher = pattern.matcher(foundComment!!.text)
            if (!matcher.find()) {
                return null
            }

            var className = matcher.group("generic")

            // Resolve the use statement
            var root = element

            while (root !is PhpFile) {
                root = root.parent
            }

            root.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is PhpUse && element.name == className) {
                        className = element.fqn
                    }

                    super.visitElement(element)
                }
            })

            return className
        }
    }
}