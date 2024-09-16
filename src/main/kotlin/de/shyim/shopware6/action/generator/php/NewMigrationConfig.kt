package de.shyim.shopware6.action.generator.php

class NewMigrationConfig(
    val name: String,
    val namespace: String,
    val timestamp: String = (System.currentTimeMillis() / 1000).toString()
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "CLASSNAME" to this.className(),
            "TIMESTAMP" to this.timestamp,
            "NAMESPACE" to this.namespace
        )
    }

    private fun className(): String {
        return "Migration" + this.timestamp + this.name
    }

    fun fileName(): String {
        return this.className() + ".php"
    }
}