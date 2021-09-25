package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class AdminComponent(
    var name: String,
    var extends: String?,
    var props: Set<String>,
    val file: String
) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.extends)
            .append(this.file)
            .append(this.props.hashCode())
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is AdminComponent &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.extends, this.extends) &&
                Objects.equals(other.props, this.props) &&
                Objects.equals(other.file, this.file)
    }
}