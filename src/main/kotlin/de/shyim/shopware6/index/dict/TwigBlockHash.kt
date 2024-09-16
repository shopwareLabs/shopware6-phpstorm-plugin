package de.shyim.shopware6.index.dict

import java.io.Serializable

class TwigBlockHash(
    val name: String,
    val relativePath: String,
    val absolutePath: String,
    val hash: String,
    val text: String
) : Serializable {
    override fun hashCode(): Int {
        return name.hashCode() + hash.hashCode() + relativePath.hashCode() + absolutePath.hashCode() + text.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is TwigBlockHash && other.name == name && other.hash == hash && other.relativePath == relativePath && other.absolutePath == absolutePath && other.text == text
    }
}