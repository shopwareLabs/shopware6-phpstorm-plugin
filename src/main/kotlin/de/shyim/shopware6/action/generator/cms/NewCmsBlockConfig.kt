package de.shyim.shopware6.action.generator.cms

import de.shyim.shopware6.index.dict.ShopwareBundle

class NewCmsBlockConfig(public val name: String, public val group: String, public val extension: ShopwareBundle) {
    fun normalizeName(): String {
        return this.name.replace("-", "_")
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            "NAME" to this.name,
            "NAME_NORMALIZED" to this.normalizeName(),
            "CATEGORY" to this.group,
        )
    }
}