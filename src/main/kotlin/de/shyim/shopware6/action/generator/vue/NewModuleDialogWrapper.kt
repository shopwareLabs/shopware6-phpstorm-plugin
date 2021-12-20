package de.shyim.shopware6.action.generator.vue

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.vue.NewModule
import javax.swing.JComponent

class NewModuleDialogWrapper: DialogWrapper(true) {
    private var panel: NewModule

    init {
        this.panel = NewModule()
    }

    override fun createCenterPanel(): JComponent? {
        return this.panel.dialog
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return panel.nameField
    }

    init {
        title = "Create a new Module"
        init()
    }

    fun showAndGetConfig(): NewModuleConfig? {
        show()

        if (!isOK || this.panel.typeField.selectedItem == null) {
            return null
        }

        val selectedItem = this.panel.typeField.selectedItem as String

        return NewModuleConfig(
            this.panel.nameField.getText(),
            selectedItem,
            this.panel.colorField.getText(),
            this.panel.iconField.getText(),
            this.panel.parentModuleField.getText(),
            this.panel.visibleInSettingsModuleCheckBox.isSelected
        )
    }
}