package de.shyim.shopware6.index

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

    override fun equals(obj: Any?): Boolean {
        return obj is SnippetFile &&
                Objects.equals(obj.snippets, this.snippets) &&
                Objects.equals(obj.file, this.file)
    }
}