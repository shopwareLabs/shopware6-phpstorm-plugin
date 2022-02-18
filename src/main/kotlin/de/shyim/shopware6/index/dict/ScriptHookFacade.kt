package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ScriptHookFacade(var name: String, var fqcn: String) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.fqcn)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ScriptHookFacade &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.fqcn, this.fqcn)
    }
}