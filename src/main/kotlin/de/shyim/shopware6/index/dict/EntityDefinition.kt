package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class EntityDefinition(val name: String, val file: String) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.file)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is EntityDefinition &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.file, this.file)
    }
}