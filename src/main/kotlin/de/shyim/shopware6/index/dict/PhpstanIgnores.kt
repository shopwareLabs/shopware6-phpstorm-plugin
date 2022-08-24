package de.shyim.shopware6.index.dict

import org.apache.commons.lang.builder.HashCodeBuilder
import java.io.Serializable
import java.util.*

class PhpstanIgnores(public var phpStanFile: String, public var errors: Int) : Serializable {
    override fun hashCode(): Int {
        return HashCodeBuilder()
            .append(this.phpStanFile)
            .append(this.errors)
            .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is PhpstanIgnores &&
                Objects.equals(other.phpStanFile, this.phpStanFile) &&
                Objects.equals(other.errors, this.errors)
    }
}