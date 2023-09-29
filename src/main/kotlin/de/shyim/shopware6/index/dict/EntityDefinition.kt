package de.shyim.shopware6.index.dict

import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class EntityDefinition(
    val name: String,
    val fqn: String,
    val file: String,
    val fields: MutableList<EntityDefinitionField>
) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.fqn)
            .append(this.file)
            .append(this.fields)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is EntityDefinition &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.fqn, this.fqn) &&
                Objects.equals(other.file, this.file) &&
                Objects.equals(other.fields, this.fields)
    }

    fun getAllAssociations(): List<EntityDefinitionField> {
        return this.fields.filter {
            it.association
        }
    }
}