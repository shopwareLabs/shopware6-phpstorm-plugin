package de.shyim.shopware6.util

import de.shyim.shopware6.index.dict.SnippetFile
import gnu.trove.THashMap
import org.codehaus.jettison.json.JSONException
import org.codehaus.jettison.json.JSONObject

object SnippetUtil {
    fun flatten(fileName: String, fileContent: String?): SnippetFile {
        val snippetList = THashMap<String, String>()

        try {
            val jsonObject = JSONObject(fileContent)
            val it = jsonObject.keys()
            while (it.hasNext()) {
                val key = it.next().toString()
                flatten(snippetList, jsonObject, key, key)
            }
        } catch (e: JSONException) {

        }

        return SnippetFile(fileName, snippetList)
    }

    private fun flatten(snippetList: THashMap<String, String>, json: JSONObject, key: String, prefix: String) {
        try {
            val jsonObject = json.getJSONObject(key)
            val it = jsonObject.keys()
            while (it.hasNext()) {
                val innerKey = it.next().toString()
                flatten(snippetList, jsonObject, innerKey, "$prefix.$innerKey")
            }
            return
        } catch (ignored: JSONException) {
        }
        try {
            snippetList[prefix] = json.getString(key)
        } catch (ignored: JSONException) {
        }
    }
}