package de.shyim.shopware6.action.generator.vue

class NewModuleConfig(var name: String, var type: String, var color: String, var icon: String, var parentModule: String, var showInSettings: Boolean) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "NAME" to this.name,
            "TYPE" to this.type,
            "COLOR" to this.color,
            "ICON" to this.icon,
            "PARENT_MODULE" to this.parentModule,
            "VISIBLE_IN_SETTINGS" to this.showInSettings.toString(),
        )
    }
}