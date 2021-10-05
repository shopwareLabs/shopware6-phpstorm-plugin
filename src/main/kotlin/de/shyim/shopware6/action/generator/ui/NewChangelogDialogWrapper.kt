package de.shyim.shopware6.action.generator.ui

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.NewChangelogDialog
import javax.swing.JComponent

class NewChangelogDialogWrapper(
    defaultTitle: String,
    defaultTicket: String,
    defaultUser: String,
    defaultEmail: String
) : DialogWrapper(true) {
    private var dialog: NewChangelogDialog

    init {
        this.dialog = NewChangelogDialog()
        this.dialog.ticketField.text = defaultTicket
        this.dialog.titleField.text = defaultTitle
        this.dialog.nameField.text = defaultUser
        this.dialog.emailField.text = defaultEmail

        setSize(400, 200)
    }

    override fun createCenterPanel(): JComponent {
        return dialog.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.titleField
    }

    init {
        title = "Create a new Changelog"
        init()
    }

    fun showAndGetConfig(): NewChangelogConfig? {
        showAndGet()

        if (!isOK) {
            return null
        }

        return NewChangelogConfig(
            this.dialog.titleField.getText(),
            this.dialog.ticketField.getText(),
            this.dialog.flagField.getText(),
            this.dialog.nameField.getText(),
            this.dialog.emailField.getText(),
            this.dialog.githubField.getText(),
        )
    }
}