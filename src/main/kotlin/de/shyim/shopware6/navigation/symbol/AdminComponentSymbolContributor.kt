package de.shyim.shopware6.navigation.symbol

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import icons.ShopwareToolBoxIcons
import de.shyim.shopware6.navigation.NavigationItemEx

class AdminComponentSymbolContributor : ChooseByNameContributor {
    override fun getNames(project: Project?, includeNonProjectItems: Boolean): Array<String> {
        val names = mutableListOf<String>()
        if (project == null) {
            return names.toTypedArray()
        }

        names.addAll(FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, project))

        return names.distinct().toTypedArray()
    }

    override fun getItemsByName(
        name: String?,
        pattern: String?,
        project: Project?,
        includeNonProjectItems: Boolean
    ): Array<NavigationItem> {
        val targets = mutableListOf<NavigationItem>()

        if (name == null || project == null) {
            return targets.toTypedArray()
        }

        val component =
            FileBasedIndex.getInstance().getValues(AdminComponentIndex.key, name, GlobalSearchScope.allScope(project))
                .firstOrNull() ?: return targets.toTypedArray()
        val file = LocalFileSystem.getInstance().findFileByPath(component.file)

        if (file != null) {
            val psi = PsiManager.getInstance(project).findFile(file)

            if (psi != null) {
                targets.add(NavigationItemEx(psi, component.name, ShopwareToolBoxIcons.SHOPWARE, "Admin Component"))
            }
        }

        return targets.toTypedArray()
    }
}