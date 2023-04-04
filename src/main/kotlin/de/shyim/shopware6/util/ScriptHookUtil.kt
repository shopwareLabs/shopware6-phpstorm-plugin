package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.ScriptHookFacadeIndex
import de.shyim.shopware6.index.ScriptHookIndex
import de.shyim.shopware6.index.dict.ScriptHook
import de.shyim.shopware6.index.dict.ScriptHookFacade

object ScriptHookUtil {
    fun getAllHooks(project: Project): MutableCollection<ScriptHook> {
        val hooks: MutableCollection<ScriptHook> = mutableListOf()

        for (key in FileBasedIndex.getInstance().getAllKeys(ScriptHookIndex.key, project)) {
            val facade = FileBasedIndex.getInstance()
                .getValues(ScriptHookIndex.key, key, GlobalSearchScope.allScope(project)).firstOrNull()

            if (facade != null) {
                hooks.add(facade)
            }
        }

        return hooks
    }

    fun getHookByName(project: Project, name: String): ScriptHook? {
        return FileBasedIndex.getInstance()
            .getValues(ScriptHookIndex.key, name, GlobalSearchScope.allScope(project)).first()
    }

    fun getAllFacades(project: Project): MutableMap<String, ScriptHookFacade> {
        val facades: MutableMap<String, ScriptHookFacade> = mutableMapOf()

        for (key in FileBasedIndex.getInstance().getAllKeys(ScriptHookFacadeIndex.key, project)) {
            val facade = FileBasedIndex.getInstance()
                .getValues(ScriptHookFacadeIndex.key, key, GlobalSearchScope.allScope(project)).firstOrNull()

            if (facade != null) {
                facades[key] = facade
            }
        }

        return facades
    }

}