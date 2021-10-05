package de.shyim.shopware6.action.generator.ui

class NewChangelogConfig(
    var title: String,
    var ticket: String,
    var flag: String,
    var user: String,
    var email: String,
    var github: String
) {
    fun toMap(): Map<String, String> {
        return mapOf(
            "TITLE" to this.title,
            "TICKET" to this.ticket,
            "FLAG" to this.flag,
            "AUTHOR" to this.user,
            "AUTHOR_EMAIL" to this.email,
            "AUTHOR_GITHUB" to this.github,
        )
    }
}