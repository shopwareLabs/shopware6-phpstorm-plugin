package de.shyim.shopware6.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FrontendSnippetIndex
import de.shyim.shopware6.util.TwigPattern
import icons.ShopwareToolBoxIcons


class TwigCompletionProvider() : CompletionContributor() {
    init {
        extend(
            CompletionType.BASIC,
            TwigPattern.getTranslationKeyPattern("trans", "transchoice"),
            object : CompletionProvider<CompletionParameters>() {
                override fun addCompletions(
                    parameters: CompletionParameters,
                    context: ProcessingContext,
                    result: CompletionResultSet
                ) {
                    val project: Project = parameters.position.project
                    for (key in FileBasedIndex.getInstance().getAllKeys(FrontendSnippetIndex.key, project)) {
                        val vals = FileBasedIndex.getInstance().getValues(FrontendSnippetIndex.key, key, GlobalSearchScope.allScope(project))

                        if (vals.size > 0) {
                            result.addElement(LookupElementBuilder.create(key).withTypeText(vals[0].first()).withIcon(ShopwareToolBoxIcons.SHOPWARE))
                        }
                    }
                }
            }
        )
    }
}