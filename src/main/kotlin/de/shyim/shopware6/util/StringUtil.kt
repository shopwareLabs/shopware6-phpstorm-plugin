package de.shyim.shopware6.util

import java.security.MessageDigest

object StringUtil {
    fun stripQuotes(str: String): String {
        return str.replace("\"", "").replace("'", "")
    }

    fun sha512(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.fold("") { str, it -> str + "%02x".format(it) }
    }
}