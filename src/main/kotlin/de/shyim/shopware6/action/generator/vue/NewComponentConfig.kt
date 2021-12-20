package de.shyim.shopware6.action.generator.vue

class NewComponentConfig(var name: String, var generateCss: Boolean, var generateTwig: Boolean) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "NAME" to this.name,
            "GENERATE_SCSS" to this.generateCss.toString(),
            "GENERATE_TWIG" to this.generateTwig.toString(),
        )
    }
}