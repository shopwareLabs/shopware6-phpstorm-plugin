package de.shyim.shopware6.action.generator.cms

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.ui.cms.NewCmsElementDialog
import javax.swing.JComponent

class NewCmsElementDialogWrapper(bundles: List<ShopwareBundle>) : DialogWrapper(true) {
    private var panel: NewCmsElementDialog = NewCmsElementDialog()

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
        title = "Create a New CMS Element"
        init()
    }

    fun showAndGetResult(): NewCmsElementConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewCmsElementConfig(
            this.panel.nameField.text.lowercase(),
            this.panel.extensionField.selectedItem as ShopwareBundle
        )
    }
}