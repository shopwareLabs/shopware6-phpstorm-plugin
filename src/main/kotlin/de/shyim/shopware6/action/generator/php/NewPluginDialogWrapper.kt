package de.shyim.shopware6.action.generator.php

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.php.NewPluginDialog
import javax.swing.JComponent

class NewPluginDialogWrapper : DialogWrapper(true) {
    private var dialog: NewPluginDialog

    init {
        this.dialog = NewPluginDialog()
        setSize(400, 200)
    }

    override fun createCenterPanel(): JComponent? {
        return this.dialog.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return this.dialog.nameField
    }

    init {
        title = "Create a new Plugin"
        init()
    }

    fun showAndGetConfig(): NewPluginConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewPluginConfig(
            dialog.nameField.text,
            dialog.namespaceField.text,
            dialog.composerName.text,
            dialog.licenseField.text,
            dialog.authorField.text,
            dialog.descriptionField.text
        )
    }
}