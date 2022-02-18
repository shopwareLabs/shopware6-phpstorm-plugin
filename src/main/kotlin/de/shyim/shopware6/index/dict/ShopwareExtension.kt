package de.shyim.shopware6.index.dict

interface ShopwareExtension {
    fun getExtensionName(): String

    fun getExtensionPath(): String

    fun getStorefrontViewFolder(): String
}