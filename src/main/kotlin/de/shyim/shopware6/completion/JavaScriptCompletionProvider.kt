package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.index.AdminSnippetIndex
import de.shyim.shopware6.index.FeatureFlagIndex
import de.shyim.shopware6.util.JavaScriptPattern
import icons.ShopwareToolBoxIcons

class JavaScriptCompletionProvider : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getComponentExtend(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    for (key in FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, project)) {
                        val vals = FileBasedIndex.getInstance()
                            .getValues(AdminComponentIndex.key, key, GlobalSearchScope.allScope(project))

                        vals.forEach {
                            result.addElement(
                                LookupElementBuilder.create(it.name)
                                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                            )
                        }
                    }
                }
            }
        )

        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getFeatureIsActive(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

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
        )

        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getTcPattern(),
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
    }
}