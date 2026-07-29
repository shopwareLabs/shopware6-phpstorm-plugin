package de.shyim.shopware6.telemetry

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages

object TelemetryConsent {
    fun requestIfNeeded(project: Project?): Boolean {
        val settings = TelemetrySettings.getInstance()

        when (settings.consent) {
            TelemetrySettings.CONSENT_ENABLED -> return true
            TelemetrySettings.CONSENT_DISABLED -> return false
        }

        val answer = Messages.showYesNoDialog(
            project,
            """
            Help improve Shopware 6 Toolbox by sending anonymous feature-usage statistics.

            We do not collect source code, file names, project names, or personal information.
            """.trimIndent(),
            "Anonymous Usage Statistics",
            "Allow",
            "No Thanks",
            Messages.getQuestionIcon(),
        )

        settings.consent =
            if (answer == Messages.YES) {
                TelemetrySettings.CONSENT_ENABLED
            } else {
                TelemetrySettings.CONSENT_DISABLED
            }

        return answer == Messages.YES
    }
}