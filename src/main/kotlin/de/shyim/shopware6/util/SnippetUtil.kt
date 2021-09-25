package de.shyim.shopware6.util

import com.intellij.json.psi.JsonProperty
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
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

    fun getTargets(psi: PsiFile, transKey: String): PsiElement {
        val snippetParts = transKey.split(".") as MutableList
        var foundPsi: PsiElement = psi

        psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                if (snippetParts.isEmpty()) {
                    return
                }

                if (element is JsonProperty) {
                    if (element.firstChild.firstChild.text == "\"" + snippetParts[0] + "\"") {
                        snippetParts.removeAt(0)

                        if (snippetParts.isEmpty()) {
                            foundPsi = element
                            return
                        }

                        super.visitElement(element)
                    }
                }

                super.visitElement(element)
            }
        })

        return foundPsi
    }
}