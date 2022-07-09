package de.shyim.shopware6.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopup
import com.intellij.openapi.ui.popup.PopupChooserBuilder
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBList
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.completion.SnippetCompletionElement
import de.shyim.shopware6.index.ShopwareBundleIndex
import de.shyim.shopware6.index.dict.ShopwareBundle
import java.awt.Component
import javax.swing.JLabel
import javax.swing.JList

object ShopwareBundleUtil {
    private val NON_VIEW_SHOPWARE_BUNDLES =
        arrayOf("Administration", "DevOps", "Checkout", "Profiling", "Elasticsearch", "Content", "System", "Framework")

    fun getAllBundles(project: Project): MutableList<ShopwareBundle> {
        val bundles: MutableList<ShopwareBundle> = ArrayList()

        for (key in FileBasedIndex.getInstance().getAllKeys(ShopwareBundleIndex.key, project)) {
            val values = FileBasedIndex.getInstance()
                .getValues(ShopwareBundleIndex.key, key, GlobalSearchScope.allScope(project))

            bundles.addAll(values)
        }

        return bundles
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
}