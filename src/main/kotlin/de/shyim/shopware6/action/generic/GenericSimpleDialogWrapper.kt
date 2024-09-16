package de.shyim.shopware6.action.generic

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.generic.GenericSimpleDialog
import javax.swing.JComponent

class GenericSimpleDialogWrapper(dialogTitle: String, fieldLabel: String, defaultFilename: String) :
    DialogWrapper(true) {
    private var dialog: GenericSimpleDialog =
        GenericSimpleDialog()

    init {
        this.title = dialogTitle
        this.dialog.fileName.text = defaultFilename
        this.dialog.fileName.name = fieldLabel

        setSize(400, 100)
    }

    override fun createCenterPanel(): JComponent {
        return dialog.contentPane
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.fileName
    }

    init {
        init()
    }

    fun showAndGetConfig(): String? {
        showAndGet()

        if (!isOK) {
            return null
        }

        return this.dialog.fileName.text
    }
}