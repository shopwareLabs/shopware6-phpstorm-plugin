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

    override fun equals(obj: Any?): Boolean {
        return obj is AdminComponent &&
                Objects.equals(obj.name, this.name) &&
                Objects.equals(obj.extends, this.extends) &&
                Objects.equals(obj.props, this.props) &&
                Objects.equals(obj.file, this.file)
    }
}