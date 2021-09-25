package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class FeatureFlag(
    var name: String,
    var default: Boolean,
    var major: Boolean,
    var description: String,
    var file: String
) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.name)
            .append(this.default)
            .append(this.major)
            .append(this.description)
            .append(this.file)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is FeatureFlag &&
                Objects.equals(other.name, this.name) &&
                Objects.equals(other.default, this.default) &&
                Objects.equals(other.major, this.major) &&
                Objects.equals(other.description, this.description) &&
                Objects.equals(other.file, this.file)
    }
}