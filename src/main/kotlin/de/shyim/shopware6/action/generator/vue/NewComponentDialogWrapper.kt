package de.shyim.shopware6.action.generator.vue

import com.intellij.openapi.ui.DialogWrapper
import de.shyim.shopware6.ui.vue.NewComponentDialog
import javax.swing.JComponent


class NewComponentDialogWrapper : DialogWrapper(true) {
    private var dialog: NewComponentDialog

    init {
        this.dialog = NewComponentDialog()
    }

    override fun createCenterPanel(): JComponent {
        return dialog.panel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return dialog.componentName
    }

    init {
        title = "Create a new Component"
        init()
    }

    fun showAndGetName(): NewComponentConfig? {
        show()

        if (!isOK) {
            return null
        }

        return NewComponentConfig(
            dialog.componentName.getText(),
            dialog.createSCSSFile.isSelected(),
            dialog.createTwigFileCheckBox.isSelected
        )
    }
}