package de.shyim.shopware6.telemetry

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class TelemetryConfigurable : Configurable {
    private var telemetryCheckbox: JBCheckBox? = null
    private var panel: JPanel? = null

    override fun getDisplayName(): String = "Shopware 6 Toolbox"

    override fun createComponent(): JComponent {
        telemetryCheckbox = JBCheckBox("Send anonymous usage statistics")

        panel = FormBuilder.createFormBuilder()
            .addComponent(telemetryCheckbox!!)
            .addComponentFillVertically(JPanel(), 0)
            .panel

        reset()

        return panel!!
    }

    override fun isModified(): Boolean {
        val telemetryEnabled =
            TelemetrySettings.getInstance().consent == TelemetrySettings.CONSENT_ENABLED

        return telemetryCheckbox?.isSelected != telemetryEnabled
    }

    override fun apply() {
        TelemetrySettings.getInstance().consent =
            if (telemetryCheckbox?.isSelected == true) {
                TelemetrySettings.CONSENT_ENABLED
            } else {
                TelemetrySettings.CONSENT_DISABLED
            }
    }

    override fun reset() {
        telemetryCheckbox?.isSelected =
            TelemetrySettings.getInstance().consent == TelemetrySettings.CONSENT_ENABLED
    }

    override fun disposeUIResources() {
        telemetryCheckbox = null
        panel = null
    }
}
