package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.SystemConfigIndex
import de.shyim.shopware6.index.dict.SystemConfig
import icons.ShopwareToolBoxIcons

object SystemConfigUtil {
    fun getAllConfigs(project: Project): MutableList<SystemConfig> {
        val configs: MutableList<SystemConfig> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(SystemConfigIndex.key, project)) {
            val vals = FileBasedIndex.getInstance()
                .getValues(SystemConfigIndex.key, key, GlobalSearchScope.allScope(project))

            configs.addAll(vals)
        }

        return configs
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllConfigs(project).forEach {
            list.add(
                LookupElementBuilder.create(it.namespace + "." + it.name).withTypeText(it.label)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }

    fun getTargets(psi: PsiFile, key: String): PsiElement {
        var foundPsi: PsiElement = psi

        psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is XmlTag) {
                    if (element.value.text == key) {
                        foundPsi = element
                        return
                    }
                }

                super.visitElement(element)
            }
        })

        return foundPsi
    }

    fun getAllNamespaceLookupItems(project: Project): MutableIterable<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllConfigs(project).forEach {
            list.add(
                LookupElementBuilder.create(it.namespace)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }
}