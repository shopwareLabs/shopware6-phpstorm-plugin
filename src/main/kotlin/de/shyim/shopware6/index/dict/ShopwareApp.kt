package de.shyim.shopware6.index.dict

import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ShopwareApp(val name: String, val rootFolder: String, val viewPath: String, val permissions: List<String>) :
    Serializable, ShopwareExtension {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.rootFolder)
            .append(this.viewPath)
            .append(this.permissions)
            .toHashCode()
    }

    override fun getExtensionName(): String {
        return this.name
    }

    override fun getExtensionPath(): String {
        return this.rootFolder
    }

    override fun getStorefrontViewFolder(): String {
        return this.viewPath
    }

    override fun equals(other: Any?): Boolean {
        return other is ShopwareApp &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.rootFolder, this.rootFolder) &&
                Objects.equals(other.viewPath, this.viewPath) &&
                Objects.equals(other.permissions, this.permissions)
    }

    override fun toString(): String {
        return this.name
    }
}