package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ShopwareBundle(val name: String, val path: String, val viewPath: String) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.path)
            .append(this.viewPath)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is ShopwareBundle &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.path, this.path) &&
                Objects.equals(other.viewPath, this.viewPath)
    }
}