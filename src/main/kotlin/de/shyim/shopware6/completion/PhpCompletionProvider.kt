package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.util.ProcessingContext
import com.jetbrains.php.lang.psi.elements.ParameterList
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression
import de.shyim.shopware6.util.FeatureFlagUtil
import de.shyim.shopware6.util.FrontendSnippetUtil
import de.shyim.shopware6.util.PHPPattern
import de.shyim.shopware6.util.SystemConfigUtil


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
                        result.addAllElements(FeatureFlagUtil.getAllLookupItems(project))
                    }

                    if (PHPPattern.isShopwareStorefrontControllerTrans(element)) {
                        result.addAllElements(FrontendSnippetUtil.getAllLookupItems(project))
                    }

                    if (PHPPattern.isShopwareCoreSystemConfigServiceGetSingle(element)) {
                        result.addAllElements(SystemConfigUtil.getAllLookupItems(project))
                    }

                    if (PHPPattern.isShopwareCoreSystemConfigServiceGetDomain(element)) {
                        result.addAllElements(SystemConfigUtil.getAllNamespaceLookupItems(project))
                    }
                }
            }
        )
    }
}