package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.shyim.shopware6.index.FeatureFlagIndex
import de.shyim.shopware6.util.PHPPattern
import icons.ShopwareToolBoxIcons


class PhpCompletionProvider : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withParent(
                PlatformPatterns.psiElement(StringLiteralExpression::class.java).inside(
                    PlatformPatterns.psiElement(ParameterList::class.java)
                )
            ),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    if (PHPPattern.isFeatureFlagFunction(element)) {
                        for (key in FileBasedIndex.getInstance().getAllKeys(FeatureFlagIndex.key, project)) {
                            val vals = FileBasedIndex.getInstance()
                                .getValues(FeatureFlagIndex.key, key, GlobalSearchScope.allScope(project))

                            vals.forEach {
                                result.addElement(
                                    LookupElementBuilder.create(it.name).withTypeText(it.description)
                                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}