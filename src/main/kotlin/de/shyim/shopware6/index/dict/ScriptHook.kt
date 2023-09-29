package de.shyim.shopware6.index.dict

import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ScriptHook(var name: String, var fqn: String, var services: List<String>, var page: String) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.fqn)
            .append(this.services)
            .append(this.page)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ScriptHook &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.fqn, this.fqn) &&
                Objects.equals(other.services, this.services) &&
                Objects.equals(other.page, this.page)
    }
}