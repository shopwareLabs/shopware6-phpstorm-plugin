package de.shyim.shopware6.installer

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.intellij.openapi.application.ApplicationInfo
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.net.URLConnection


object ShopwareApiUtil {
    fun getAllVersions(): List<ShopwareVersion> {
        var versions: MutableList<ShopwareVersion> = ArrayList()

        val jsonContent = getAllVersionsRaw() ?: return versions

        val jsonObject: JsonArray = JsonParser.parseString(jsonContent).asJsonArray ?: return versions

        jsonObject.forEach {
            val version = it as JsonPrimitive

            versions.add(ShopwareVersion(version.asString))
        }

        versions = versions
            .filter {
                Integer.parseInt(it.name.split(".")[1]) >= 5
            }
            .sortedBy {
                it.name.split(".").map { it.padStart(10, '0') }.joinToString(".")
            }
            .reversed()
            .toMutableList()

        return versions
    }

    private fun getAllVersionsRaw(): String? {
        val userAgent = String.format(
            "%s / %s",
            ApplicationInfo.getInstance().versionName,
            ApplicationInfo.getInstance().build
        )

        return try {
            val url = URI("https://api.shopware.com/platform/releases").toURL()
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