package de.shyim.shopware6.action.generator.cms

import de.shyim.shopware6.index.dict.ShopwareBundle

class NewCmsBlockConfig(val name: String, val group: String, val extension: ShopwareBundle) {
    private fun normalizeName(): String {
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