package de.shyim.shopware6.index.dict

import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ThemeConfig(val name: String, val label: String, val value: String, val file: String) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.label)
            .append(this.value)
            .append(this.file)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ThemeConfig &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.label, this.label) &&
                Objects.equals(other.value, this.value) &&
                Objects.equals(other.file, this.file)
    }
}