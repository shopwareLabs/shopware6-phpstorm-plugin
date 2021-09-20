package de.shyim.shopware6.action.generator.ui

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField


class NewComponentDialogWrapper : DialogWrapper(true) {
    private var textField: JTextField

    init {
        textField = JTextField()
    }

    override fun createCenterPanel(): JComponent {
        val dialogPanel = JPanel(BorderLayout())
        dialogPanel.add(textField, BorderLayout.CENTER)

        return dialogPanel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return textField
    }

    init {
        title = "Create a new Component"
        init()
    }

    fun showAndGetName(): String
    {
        show();

        return textField.getText()
    }
}