package de.shyim.shopware6.util

import gnu.trove.THashMap
import org.codehaus.jettison.json.JSONException
import org.codehaus.jettison.json.JSONObject

object SnippetUtil {
    fun flatten(fileContent: String?): THashMap<String, Set<String>> {
        val content = THashMap<String, Set<String>>()
        return try {
            val jsonObject = JSONObject(fileContent)
            val it = jsonObject.keys()
            while (it.hasNext()) {
                val key = it.next().toString()
                flatten(content, jsonObject, key, key)
            }
            content
        } catch (e: JSONException) {
            e.printStackTrace()
            content
        }
    }

    private fun flatten(content: THashMap<String, Set<String>>, json: JSONObject, key: String, prefix: String) {
        try {
            val jsonObject = json.getJSONObject(key)
            val it = jsonObject.keys()
            while (it.hasNext()) {
                val innerKey = it.next().toString()
                flatten(content, jsonObject, innerKey, "$prefix.$innerKey")
            }
            return
        } catch (ignored: JSONException) {
        }
        try {
            content[prefix] = setOf(json.getString(key))
        } catch (ignored: JSONException) {
        }
    }
}