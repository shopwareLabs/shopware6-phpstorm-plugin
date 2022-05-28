package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminMixinIndex
import de.shyim.shopware6.index.dict.AdminMixin
import icons.ShopwareToolBoxIcons

object AdminMixinUtil {
    private fun getAllMixins(project: Project): MutableList<AdminMixin> {
        val definitions: MutableList<AdminMixin> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(AdminMixinIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(AdminMixinIndex.key, key, GlobalSearchScope.allScope(project))

            definitions.addAll(values)
        }

        return definitions
    }

    fun getAllLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllMixins(project).forEach {
            list.add(
                LookupElementBuilder.create(it.name)
                    .withIcon(ShopwareToolBoxIcons.SHOPWARE)
            )
        }

        return list
    }

    fun getTargets(project: Project, key: String): MutableCollection<PsiElement> {
        val psiElements: MutableList<PsiElement> = java.util.ArrayList()

        getAllMixins(project).forEach {
            if (it.name == key) {
                val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                if (file != null) {
                    val psi = PsiManager.getInstance(project).findFile(file)

                    if (psi != null) {
                        psiElements.add(psi)
                    }
                }
            }
        }

        return psiElements
    }
}