package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.json.psi.JsonProperty
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ThemeConfigIndex
import de.shyim.shopware6.index.dict.ThemeConfig
import icons.ShopwareToolBoxIcons

object ThemeConfigUtil {
    fun getAllConfigs(project: Project): MutableList<ThemeConfig> {
        val flags: MutableList<ThemeConfig> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(ThemeConfigIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(ThemeConfigIndex.key, key, GlobalSearchScope.allScope(project))

            flags.addAll(values)
        }

        return flags
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllConfigs(project).forEach {
            list.add(
                LookupElementBuilder.create(it.name).withTypeText(it.label + " " + it.value)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }

    fun getTargets(psi: PsiFile, key: String): PsiElement {
        var foundPsi: PsiElement = psi
        val searchKey = "\"" + key + "\""

        psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is JsonProperty) {
                    if (element.firstChild.text == searchKey) {
                        foundPsi = element
                        return
                    }
                }

                super.visitElement(element)
            }
        })

        return foundPsi
    }
}