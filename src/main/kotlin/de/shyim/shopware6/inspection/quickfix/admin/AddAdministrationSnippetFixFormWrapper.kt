package de.shyim.shopware6.inspection.quickfix.admin

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiFile
import de.shyim.shopware6.ui.snippet.CreateSnippetsForm
import java.awt.Component
import javax.swing.*


class AddAdministrationSnippetFixFormWrapper(private val key: String, private val files: Collection<PsiFile>) :
    DialogWrapper(true) {
    private var dialog: CreateSnippetsForm
    private var inputs: MutableMap<String, JTextField>
    private val snippetKeyInput: JTextField

    init {
        title = "Create a Snippet"
        this.dialog = CreateSnippetsForm()
        this.dialog.panel.layout = BoxLayout(this.dialog.panel, BoxLayout.Y_AXIS)
        this.dialog.panel.alignmentY = Component.LEFT_ALIGNMENT
        setSize(400, 200)
        init()

        this.snippetKeyInput = JTextField(this.key)
        addAndLeftAlign(this.dialog.panel, JLabel("Key: "))
        this.dialog.panel.add(this.snippetKeyInput)

        this.inputs = mutableMapOf()

        this.files.forEach { file ->
            val label = JLabel("${file.name}: ")

            val textField = JTextField(this.key)
            this.inputs[file.name] = textField
            label.labelFor = textField

            addAndLeftAlign(this.dialog.panel, label)
            this.dialog.panel.add(textField)
        }
    }

    private fun addAndLeftAlign(parent: JComponent, child: JComponent) {
        val b = Box.createHorizontalBox()
        b.add(child)
        b.add(Box.createHorizontalGlue())
        parent.add(b)
    }

    override fun createCenterPanel(): JComponent {
        return dialog.panel
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return this.snippetKeyInput
    }

    fun showAndGetInfo(): AddAdministrationSnippetFixConfig? {
        showAndGet()

        if (!isOK) {
            return null
        }

        val translationMapping: MutableMap<String, String> = mutableMapOf()

        this.inputs.forEach { (t, u) ->
            translationMapping[t] = u.text
        }

        return AddAdministrationSnippetFixConfig(this.snippetKeyInput.text, translationMapping)
    }
}