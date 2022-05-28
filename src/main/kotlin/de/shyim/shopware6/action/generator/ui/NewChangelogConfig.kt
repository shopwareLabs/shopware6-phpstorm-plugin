package de.shyim.shopware6.action.generator.ui

class NewChangelogConfig(
    var title: String,
    private var ticket: String,
    private var flag: String,
    private var user: String,
    private var email: String,
    private var github: String
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