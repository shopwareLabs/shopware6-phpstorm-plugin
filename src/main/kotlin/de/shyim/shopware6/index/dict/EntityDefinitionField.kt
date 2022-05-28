package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class EntityDefinitionField(
    val name: String,
    val type: String,
    val association: Boolean,
    val associationTarget: String = ""
) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.type)
            .append(this.association)
            .append(this.associationTarget)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is EntityDefinitionField &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.type, this.type) &&
                Objects.equals(other.association, this.association) &&
                Objects.equals(other.associationTarget, this.associationTarget)
    }
}