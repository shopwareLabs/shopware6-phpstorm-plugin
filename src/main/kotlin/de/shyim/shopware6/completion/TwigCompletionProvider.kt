package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.util.ProcessingContext
import de.shyim.shopware6.util.*
import icons.ShopwareToolBoxIcons


class TwigCompletionProvider : CompletionContributor() {
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

                    result.addAllElements(FrontendSnippetUtil.getAllLookupItems(project))
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
                    val project: Project = parameters.position.project

                    result.addAllElements(FeatureFlagUtil.getAllLookupItems(project))
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

                    result.addAllElements(AdminSnippetUtil.getAllLookupItems(project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            TwigPattern.getTagTokenParserPattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    result.addElement(
                        LookupElementBuilder.create("sw_include").withIcon(ShopwareToolBoxIcons.SHOPWARE)
                    )
                    result.addElement(
                        LookupElementBuilder.create("sw_extends").withIcon(ShopwareToolBoxIcons.SHOPWARE)
                    )
                }
            }
        )

        extend(
            CompletionType.BASIC,
            TwigPattern.getPrintBlockOrTagFunctionPattern("theme_config"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project

                    result.addAllElements(ThemeConfigUtil.getAllLookupItems(project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            TwigPattern.getPrintBlockOrTagFunctionPattern("config"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project

                    result.addAllElements(SystemConfigUtil.getAllLookupItems(project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            TwigPattern.getScriptRepositorySearchPattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project

                    result.addAllElements(EntityDefinitionUtil.getAllLookupItems(project))
                    result.stopHere()
                }
            }
        )
    }
}