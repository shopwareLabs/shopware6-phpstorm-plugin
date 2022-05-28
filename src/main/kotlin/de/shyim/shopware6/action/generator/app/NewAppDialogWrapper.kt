package de.shyim.shopware6.action.generator.app

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.app.NewAppDialog
import javax.swing.JComponent

class NewAppDialogWrapper : DialogWrapper(true) {
    private var dialog: NewAppDialog = NewAppDialog()

    override fun createCenterPanel(): JComponent {
        return this.dialog.dialog
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.nameField
    }

    init {
        title = "Create a New Shopware App"
        init()
    }

    fun showAndGetName(): NewAppConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewAppConfig(
            dialog.nameField.text,
            dialog.labelField.text,
            dialog.authorField.text,
            dialog.licenseField.text,
        )
    }
}