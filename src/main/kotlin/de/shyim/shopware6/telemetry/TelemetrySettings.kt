package de.shyim.shopware6.telemetry

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.RoamingType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.util.UUID

@Service(Service.Level.APP)
@State(
    name = "ShopwareToolboxTelemetry",
    storages = [
        Storage(
            value = "shopware6-toolbox-telemetry.xml",
            roamingType = RoamingType.DISABLED,
        ),
    ],
)
class TelemetrySettings : PersistentStateComponent<TelemetrySettings.SettingsState> {
    data class SettingsState(
        var consent: String = CONSENT_UNKNOWN,
        var installationId: String = UUID.randomUUID().toString(),
    )

    private var settingsState = SettingsState()

    override fun getState(): SettingsState = settingsState

    override fun loadState(state: SettingsState) {
        settingsState = state
    }

    var consent: String
        get() = settingsState.consent
        set(value) {
            settingsState.consent = value
        }

    val installationId: String
        get() = settingsState.installationId

    companion object {
        const val CONSENT_UNKNOWN = "unknown"
        const val CONSENT_ENABLED = "enabled"
        const val CONSENT_DISABLED = "disabled"

        fun getInstance(): TelemetrySettings =
            ApplicationManager.getApplication().getService(TelemetrySettings::class.java)
    }
}