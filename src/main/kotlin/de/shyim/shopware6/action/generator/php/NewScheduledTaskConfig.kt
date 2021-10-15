package de.shyim.shopware6.action.generator.php

class NewScheduledTaskConfig(val name: String, val taskName: String, val interval: String, val namespace: String) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "NAME" to this.name,
            "TASKNAME" to this.taskName,
            "INTERVAL" to this.interval,
            "NAMESPACE" to this.namespace
        )
    }
}