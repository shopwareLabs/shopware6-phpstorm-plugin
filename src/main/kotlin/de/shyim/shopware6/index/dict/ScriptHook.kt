package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ScriptHook(var name: String, var fqcn: String, var services: List<String>) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.fqcn)
            .append(this.services)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ScriptHook &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.fqcn, this.fqcn) &&
                Objects.equals(other.services, this.services)
    }
}