package de.shyim.shopware6.installer

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.application.ApplicationInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection


object ShopwareApiUtil {
    fun getAllVersions(): List<ShopwareVersion> {
        val versions: MutableList<ShopwareVersion> = ArrayList<ShopwareVersion>()

        val jsonContent = getAllVersionsRaw() ?: return versions

        val jsonObject: JsonArray = JsonParser.parseString(jsonContent).asJsonArray ?: return versions

        jsonObject.forEach {
            val version = it as JsonObject

            versions.add(ShopwareVersion(version.get("version").asString, version.get("uri").asString))
        }

        return versions
    }

    private fun getAllVersionsRaw(): String? {
        val userAgent = String.format(
            "%s / %s",
            ApplicationInfo.getInstance().versionName,
            ApplicationInfo.getInstance().build
        )

        return try {
            val url = URL("https://update-api.shopware.com/v1/releases/install?major=6")
            val conn: URLConnection = url.openConnection()
            conn.setRequestProperty("User-Agent", userAgent)
            conn.connect()
            val `in` = BufferedReader(InputStreamReader(conn.getInputStream()))
            var content: String? = ""
            var line: String?
            while (`in`.readLine().also { line = it } != null) {
                content += line
            }
            `in`.close()
            content!!
        } catch (e: IOException) {
            null
        }
    }
}