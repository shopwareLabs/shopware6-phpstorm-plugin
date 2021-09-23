package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FeatureFlagIndex
import de.shyim.shopware6.index.FrontendSnippetIndex
import de.shyim.shopware6.util.TwigPattern
import icons.ShopwareToolBoxIcons


class TwigCompletionProvider() : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            TwigPattern.getTranslationKeyPattern("trans"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project
                    for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
                        val vals = FileBasedIndex.getInstance()
                            .getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

                        vals.forEach {
                            it.snippets.forEach {
                                result.addElement(
                                    LookupElementBuilder.create(it.key).withTypeText(it.value)
                                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                                )
                            }
                        }
                    }
                }
            }
        )

        extend(
            CompletionType.BASIC,
            TwigPattern.getPrintBlockOrTagFunctionPattern("feature")!!,
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    for (key in FileBasedIndex.getInstance()
                        .getAllKeys(FeatureFlagIndex.key, parameters.position.project)) {
                        val vals = FileBasedIndex.getInstance()
                            .getValues(
                                FeatureFlagIndex.key,
                                key,
                                GlobalSearchScope.allScope(parameters.position.project)
                            )

                        vals.forEach {
                            result.addElement(
                                LookupElementBuilder.create(it.name).withTypeText(it.description)
                                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                            )
                        }
                    }
                }
            }
        )
    }
}