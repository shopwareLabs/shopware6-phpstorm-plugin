package de.shyim.shopware6.index.dict

import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class TwigDeprecation(val name: String, val relPath: String, val message: String) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.relPath)
            .append(this.message)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is TwigDeprecation &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.relPath, this.relPath) &&
                Objects.equals(other.message, this.message)
    }
}