package de.shyim.shopware6.symfonyplugin

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.util.ProcessingContext
import de.shyim.shopware6.util.TwigPattern
import fr.adrienbrault.idea.symfony2plugin.routing.RouteHelper

class SymfonyTwigCompletionProvider : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            TwigPattern.getPrintBlockOrTagFunctionPattern("seoUrl", "sw_csrf"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    result.addAllElements(RouteHelper.getRoutesLookupElements(parameters.position.project))
                }
            }
        )

    }
}