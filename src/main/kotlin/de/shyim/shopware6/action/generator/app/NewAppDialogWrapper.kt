package de.shyim.shopware6.action.generator.app

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.app.NewAppDialog
import javax.swing.JComponent

class NewAppDialogWrapper : DialogWrapper(true) {
    private var dialog: NewAppDialog

    init {
        this.dialog = NewAppDialog()
    }

    override fun createCenterPanel(): JComponent {
        return this.dialog.dialog
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.nameField
    }

    init {
        title = "Create a new Shopware App"
        init()
    }

    fun showAndGetName(): NewAppConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewAppConfig(
            dialog.nameField.getText(),
            dialog.labelField.getText(),
            dialog.authorField.getText(),
            dialog.licenseField.getText(),
        )
    }
}