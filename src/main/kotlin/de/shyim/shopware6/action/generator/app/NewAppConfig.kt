package de.shyim.shopware6.action.generator.app

class NewAppConfig(var name: String, var label: String, private var author: String, private var license: String) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "NAME" to this.name,
            "LABEL" to this.label,
            "AUTHOR" to this.author,
            "LICENSE" to this.license,
        )
    }
}
