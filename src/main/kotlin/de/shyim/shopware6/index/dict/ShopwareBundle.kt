package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class ShopwareBundle(
    val name: String,
    val path: String,
    val viewPath: String,
    val composerFolder: String,
    val rootFolder: String
) : Serializable,
    ShopwareExtension {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.path)
            .append(this.viewPath)
            .append(this.composerFolder)
            .append(this.rootFolder)
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
        return other is ShopwareBundle &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.path, this.path) &&
                Objects.equals(other.composerFolder, this.composerFolder) &&
                Objects.equals(other.viewPath, this.viewPath) &&
                Objects.equals(other.rootFolder, this.rootFolder)
    }

    override fun toString(): String {
        return this.name
    }
}