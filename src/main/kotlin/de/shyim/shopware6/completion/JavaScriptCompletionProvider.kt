package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.util.*
import icons.ShopwareToolBoxIcons

class JavaScriptCompletionProvider : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getComponentPattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val element = parameters.originalPosition ?: return
                    val project = element.project

                    for (key in FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, project)) {
                        val values = FileBasedIndex.getInstance()
                            .getValues(AdminComponentIndex.key, key, GlobalSearchScope.allScope(project))

                        values.forEach {
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
                    val project: Project = parameters.position.project

                    result.addAllElements(FeatureFlagUtil.getAllLookupItems(project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getTranslationPattern(),
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
            JavaScriptPattern.getRepositoryFactoryCreatePattern(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project

                    result.addAllElements(EntityDefinitionUtil.getAllLookupItems(project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getMixinGetByName(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {

                    val project: Project = parameters.position.project

                    result.addAllElements(AdminMixinUtil.getAllLookupItems(project))
                }
            }
        )

        extend(
            CompletionType.BASIC,
            JavaScriptPattern.getRouteCompletion(),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project

                    result.addAllElements(AdminModuleUtil.getAllRouteLookupItems(project))
                }
            }
        )
    }
}