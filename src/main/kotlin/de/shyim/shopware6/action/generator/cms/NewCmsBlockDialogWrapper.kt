package de.shyim.shopware6.action.generator.cms

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.ui.cms.NewCmsBlockDialog
import javax.swing.JComponent

class NewCmsBlockDialogWrapper(bundles: List<ShopwareBundle>) : DialogWrapper(true) {
    private var panel: NewCmsBlockDialog = NewCmsBlockDialog()

    init {
        bundles.forEach { shopwareBundle ->
            this.panel.extensionField.addItem(shopwareBundle)
        }

        setSize(400, 200)
    }

    override fun createCenterPanel(): JComponent? {
        return this.panel.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return this.panel.extensionField
    }

    init {
        title = "Create a new CMS block"
        init()
    }

    fun showAndGetResult(): NewCmsBlockConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewCmsBlockConfig(
            this.panel.nameField.text.lowercase(),
            this.panel.groupField.selectedItem as String,
            this.panel.extensionField.selectedItem as ShopwareBundle
        )
    }
}