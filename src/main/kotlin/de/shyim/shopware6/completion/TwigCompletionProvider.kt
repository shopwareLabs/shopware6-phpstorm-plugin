package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminSnippetIndex
import de.shyim.shopware6.index.FeatureFlagIndex
import de.shyim.shopware6.index.FrontendSnippetIndex
import de.shyim.shopware6.util.TwigPattern
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent
import fr.adrienbrault.idea.symfony2plugin.routing.RouteHelper
import fr.adrienbrault.idea.symfony2plugin.templating.util.TwigUtil
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
            TwigPattern.getPrintBlockOrTagFunctionPattern("feature"),
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

        extend(
            CompletionType.BASIC,
            TwigPattern.getTcPattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project
                    for (key in FileBasedIndex.getInstance().getAllKeys(AdminSnippetIndex.key, project)) {
                        val vals = FileBasedIndex.getInstance()
                            .getValues(AdminSnippetIndex.key, key, GlobalSearchScope.allScope(project))

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
            TwigPattern.getPrintBlockOrTagFunctionPattern("seoUrl"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    if (!Symfony2ProjectComponent.isEnabled(parameters.position)) {
                        return
                    }

                    result.addAllElements(RouteHelper.getRoutesLookupElements(parameters.position.project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            TwigPattern.getShopwareIncludeExtendsTagPattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    if (!Symfony2ProjectComponent.isEnabled(parameters.position)) {
                        return
                    }

                    result.addAllElements(TwigUtil.getTwigLookupElements(parameters.getPosition().getProject()));
                }
            }
        )

        extend(
            CompletionType.BASIC,
            fr.adrienbrault.idea.symfony2plugin.templating.TwigPattern.getTagTokenParserPattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    if (!Symfony2ProjectComponent.isEnabled(parameters.position)) {
                        return
                    }

                    result.addElement(
                        LookupElementBuilder.create("sw_include").withIcon(ShopwareToolBoxIcons.SHOPWARE)
                    );
                    result.addElement(
                        LookupElementBuilder.create("sw_extends").withIcon(ShopwareToolBoxIcons.SHOPWARE)
                    );
                }
            }
        )
    }
}