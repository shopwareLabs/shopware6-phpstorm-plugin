package de.shyim.shopware6.action.generator.php

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.php.NewMigrationDialog.NewMigrationDialog
import javax.swing.JComponent

class NewMigrationDialogWrapper(namespace: String) : DialogWrapper(true) {
    private var dialog: NewMigrationDialog = NewMigrationDialog()

    init {
        this.dialog.namespaceField.text = namespace
        setSize(400, 200)
    }

    override fun createCenterPanel(): JComponent {
        return dialog.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.nameField
    }

    init {
        title = "Create a New Migration"
        init()
    }

    fun showAndGetConfig(): NewMigrationConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewMigrationConfig(
            dialog.nameField.text,
            dialog.namespaceField.text,
        )
    }
}