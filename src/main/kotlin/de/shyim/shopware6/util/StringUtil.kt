package de.shyim.shopware6.util

object StringUtil {
    fun stripQuotes(str: String): String {
        return str.replace("\"", "").replace("'", "")
    }
}