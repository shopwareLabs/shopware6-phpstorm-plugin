package de.shyim.shopware6.util

import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.index.AdminModuleIndex
import de.shyim.shopware6.index.dict.AdminModule
import icons.ShopwareToolBoxIcons

object AdminModuleUtil {
    private fun getAllModules(project: Project): MutableList<AdminModule> {
        val modules: MutableList<AdminModule> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(AdminModuleIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(AdminModuleIndex.key, key, GlobalSearchScope.allScope(project))

            modules.addAll(values)
        }

        return modules
    }

    fun getAllRouteLookupItems(project: Project): MutableList<LookupElement> {
        val list: MutableList<LookupElement> = ArrayList()

        getAllModules(project).forEach { module ->
            module.routes.forEach { route ->
                list.add(
                    LookupElementBuilder.create(route.key)
                        .withIcon(ShopwareToolBoxIcons.SHOPWARE)
                )
            }
        }

        return list
    }

    fun getRoutePsiTargets(route: String, project: Project): MutableList<PsiElement> {
        val psiElements: MutableList<PsiElement> = ArrayList()

        getAllModules(project).forEach { module ->
            if (module.routes.containsKey(route)) {
                PsiUtil.addPsiFileToList(module.file, project, psiElements)

                if (module.routes[route]!!.component != null) {
                    val component = FileBasedIndex.getInstance().getValues(
                        AdminComponentIndex.key,
                        module.routes[route]!!.component!!,
                        GlobalSearchScope.allScope(project)
                    ).firstOrNull()

                    if (component != null) {
                        PsiUtil.addPsiFileToList(component.file, project, psiElements)
                    }
                }
            }
        }

        return psiElements
    }
}