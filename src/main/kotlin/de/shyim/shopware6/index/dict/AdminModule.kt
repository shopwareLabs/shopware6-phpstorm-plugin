package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class AdminModule(
    var name: String,
    var file: String,
    var routes: MutableMap<String, AdminModuleRoute> = mutableMapOf()
) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.file)
            .append(this.routes)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is AdminModule &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.file, this.file) &&
                Objects.equals(other.routes, this.routes)
    }
}