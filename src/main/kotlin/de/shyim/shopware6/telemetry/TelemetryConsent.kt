package de.shyim.shopware6.telemetry

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.util.concurrent.atomic.AtomicBoolean

object TelemetryConsent {
    private val consentRequested = AtomicBoolean(false)

    fun requestIfNeeded(project: Project?): Boolean {
        val settings = TelemetrySettings.getInstance()

        when (settings.consent) {
            TelemetrySettings.CONSENT_ENABLED -> return true
            TelemetrySettings.CONSENT_DISABLED -> return false
        }

        if (consentRequested.compareAndSet(false, true)) {
            showConsentNotification(project)
        }

        return false
    }

    private fun showConsentNotification(project: Project?) {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("Shopware Telemetry")
            .createNotification(
                "Anonymous usage statistics",
                "Help improve Shopware 6 Toolbox by sending anonymous feature-usage statistics. " +
                    "We do not collect source code, file names, project names, or personal information. " +
                    "You can change this anytime in Settings | Tools | Shopware 6 Toolbox.",
                NotificationType.INFORMATION,
            )

        notification.addAction(NotificationAction.createSimpleExpiring("Allow") {
            TelemetrySettings.getInstance().consent = TelemetrySettings.CONSENT_ENABLED
        })

        notification.addAction(NotificationAction.createSimpleExpiring("No thanks") {
            TelemetrySettings.getInstance().consent = TelemetrySettings.CONSENT_DISABLED
        })

        notification.notify(project)
    }
}
