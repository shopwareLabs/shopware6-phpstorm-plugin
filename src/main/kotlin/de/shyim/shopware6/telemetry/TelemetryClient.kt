package de.shyim.shopware6.telemetry

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.SystemInfo
import org.codehaus.jettison.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.charset.StandardCharsets
import java.time.Instant

@Service(Service.Level.APP)
class TelemetryClient {
    fun track(
        feature: String,
        result: String,
        durationMs: Long,
    ) {
        val settings = TelemetrySettings.getInstance()

        if (settings.consent != TelemetrySettings.CONSENT_ENABLED) {
            return
        }

        if (System.getenv().containsKey("DO_NOT_TRACK")) {
            return
        }

        ApplicationManager.getApplication().executeOnPooledThread {
            runCatching {
                sendEvent(feature, result, durationMs, settings.installationId)
            }
        }
    }

    private fun sendEvent(
        feature: String,
        result: String,
        durationMs: Long,
        installationId: String,
    ) {
        val tags = JSONObject().apply {
            put("feature", feature)
            put("result", result)
            put("plugin_version", pluginVersion())
            put("ide_version", ApplicationInfo.getInstance().fullVersion)
            put("os", operatingSystem())
            put("duration_ms", durationMs.toString())
        }

        val payload = JSONObject().apply {
            put("event", EVENT_NAME)
            put("user_id", installationId)
            put("timestamp", Instant.now().toString())
            put("tags", tags)
        }

        val bytes = payload.toString().toByteArray(StandardCharsets.UTF_8)
        val domain = System.getenv("SHOPWARE_TRACKING_DOMAIN")
            ?.takeIf { it.isNotBlank() }
            ?: DEFAULT_DOMAIN

        DatagramSocket().use { socket ->
            val address = InetAddress.getByName(domain)
            val packet = DatagramPacket(bytes, bytes.size, address, DEFAULT_PORT)
            socket.send(packet)
        }
    }

    private fun pluginVersion(): String =
        PluginManagerCore
            .getPlugin(PluginId.getId(PLUGIN_ID))
            ?.version
            ?: "unknown"

    private fun operatingSystem(): String =
        when {
            SystemInfo.isMac -> "macos"
            SystemInfo.isWindows -> "windows"
            SystemInfo.isLinux -> "linux"
            else -> "other"
        }

    companion object {
        private const val EVENT_NAME = "shopware_phpstorm.feature_used"
        private const val PLUGIN_ID = "de.shyim.shopware6"
        private const val DEFAULT_DOMAIN = "udp.usage.shopware.io"
        private const val DEFAULT_PORT = 9000

        fun getInstance(): TelemetryClient =
            ApplicationManager.getApplication().getService(TelemetryClient::class.java)
    }
}