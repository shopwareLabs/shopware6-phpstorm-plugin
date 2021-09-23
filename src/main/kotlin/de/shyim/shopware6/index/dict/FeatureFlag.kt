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

    override fun equals(obj: Any?): Boolean {
        return obj is FeatureFlag &&
                Objects.equals(obj.name, this.name) &&
                Objects.equals(obj.default, this.default) &&
                Objects.equals(obj.major, this.major) &&
                Objects.equals(obj.description, this.description) &&
                Objects.equals(obj.file, this.file)
    }
}