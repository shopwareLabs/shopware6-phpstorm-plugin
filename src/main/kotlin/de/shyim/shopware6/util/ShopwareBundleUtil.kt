package de.shyim.shopware6.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBList
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.index.AdminComponentOverrideIndex
import de.shyim.shopware6.index.ShopwareBundleIndex
import de.shyim.shopware6.index.dict.AdminComponent
import de.shyim.shopware6.index.dict.ShopwareBundle
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

object ShopwareBundleUtil {
    private val NON_VIEW_SHOPWARE_BUNDLES =
        arrayOf("Administration", "DevOps", "Checkout", "Profiling", "Elasticsearch", "Content", "System", "Framework")

    fun getAllBundles(project: Project): MutableList<ShopwareBundle> {
        return FileBasedIndex.getInstance()
            .getValues(ShopwareBundleIndex.key, "all", GlobalSearchScope.allScope(project))
    }

    fun getAllBundlesRelatedToViews(project: Project): List<ShopwareBundle> {
        return getAllBundles(project).filter {
            !NON_VIEW_SHOPWARE_BUNDLES.contains(it.name)
        }
    }

    fun getBundleSelectionPopup(project: Project): PopupChooserBuilder<ShopwareBundle> {
        val popupList = JBList(getAllBundles(project))

        popupList.cellRenderer = object : JBList.StripedListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                if (renderer is JLabel && value is ShopwareBundle) {
                    renderer.text = value.name
                }

                return renderer
            }
        }

        return PopupChooserBuilder(popupList)
            .setTitle("Shopware: Select Bundle")
            .setFilteringEnabled {
                return@setFilteringEnabled (it as ShopwareBundle).name
            }
    }

    fun getAllComponentsInBundle(bundle: ShopwareBundle, project: Project): MutableList<AdminComponent> {
        val components = mutableListOf<AdminComponent>()

        FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, project).forEach { key ->
            FileBasedIndex.getInstance().getValues(AdminComponentIndex.key, key, GlobalSearchScope.projectScope(project)).forEach { component ->
                if (component.file.startsWith(bundle.rootFolder)) {
                    components.add(component)
                }
            }
        }

        return components
    }

    fun getAllComponentsWithOverridesInBundle(bundle: ShopwareBundle, project: Project): MutableList<AdminComponent> {
        val components = getAllComponentsInBundle(bundle, project)

        FileBasedIndex.getInstance().getAllKeys(AdminComponentOverrideIndex.key, project).forEach { key ->
            FileBasedIndex.getInstance().getValues(AdminComponentOverrideIndex.key, key, GlobalSearchScope.projectScope(project)).forEach { component ->
                if (component.file.startsWith(bundle.rootFolder)) {
                    components.add(AdminComponent("override: ${component.name}", component.name, "override", HashSet(), component.file))
                }
            }
        }

        return components
    }
}