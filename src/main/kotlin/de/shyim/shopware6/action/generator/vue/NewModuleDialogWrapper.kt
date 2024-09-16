package de.shyim.shopware6.action.generator.vue

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.vue.NewModule
import javax.swing.JComponent

class NewModuleDialogWrapper : DialogWrapper(true) {
    private var panel: NewModule = NewModule()

    override fun createCenterPanel(): JComponent? {
        return this.panel.dialog
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return panel.nameField
    }

    init {
        title = "Create a New Module"
        init()
    }

    fun showAndGetConfig(): NewModuleConfig? {
        show()

        if (!isOK || this.panel.typeField.selectedItem == null) {
            return null
        }

        val selectedItem = this.panel.typeField.selectedItem as String

        return NewModuleConfig(
            this.panel.nameField.text,
            selectedItem,
            this.panel.colorField.text,
            this.panel.iconField.text,
            this.panel.parentModuleField.text,
            this.panel.visibleInSettingsModuleCheckBox.isSelected
        )
    }
}