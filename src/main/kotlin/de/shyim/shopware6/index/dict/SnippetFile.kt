package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class SnippetFile(var file: String, var snippets: Map<String, String>) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.file)
            .append(this.snippets)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is SnippetFile &&
                Objects.equals(other.snippets, this.snippets) &&
                Objects.equals(other.file, this.file)
    }
}