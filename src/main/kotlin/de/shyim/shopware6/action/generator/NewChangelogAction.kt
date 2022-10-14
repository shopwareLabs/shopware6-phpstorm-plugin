package de.shyim.shopware6.action.generator

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.DumbAwareAction
import de.shyim.shopware6.action.generator.ui.NewChangelogDialogWrapper
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.templates.ShopwareTemplates.Companion.SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE
import git4idea.GitUserRegistry
import git4idea.branch.GitBranchUtil
import org.apache.commons.lang.StringUtils
import java.text.SimpleDateFormat
import java.util.*

class NewChangelogAction: DumbAwareAction("Create a Changelog", "Create a new Changelog file", AllIcons.FileTypes.Text)  {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val viewDirectory = ActionUtil.getViewDirectory(e.dataContext) ?: return

        val info = GitUserRegistry.getInstance(e.project!!).getOrReadUser(viewDirectory.virtualFile)
        val currentBranch = GitBranchUtil.getCurrentRepository(e.project!!)?.currentBranch
        var defaultTitle = ""
        var defaultTicket = ""
        var defaultUser = ""
        var defaultEmail = ""

        if (currentBranch != null && currentBranch.name.startsWith("next-")) {
            val branchParts = currentBranch.name.split("/")

            if (branchParts.count() == 2) {
                defaultTicket = branchParts[0].uppercase()
                defaultTitle = StringUtils.capitalize(branchParts[1]).replace("-", " ")
            }
        }

        if (info != null) {
            defaultUser = info.name
            defaultEmail = info.email
        }

        val dialog = NewChangelogDialogWrapper(defaultTitle, defaultTicket, defaultUser, defaultEmail)
        val dialogResult = dialog.showAndGetConfig() ?: return

        val date = Date()
        val modifiedDate: String = SimpleDateFormat("yyyy-MM-dd").format(date)

        var fileName = modifiedDate + "-" + dialogResult.title.lowercase()

        fileName = "[^a-z_\\-0-9]".toRegex().replace(fileName, "-")
        fileName = "[-]{2,}".toRegex().replace(fileName, "-")
        fileName = "[-_]+\$".toRegex().replace(fileName, "")
        fileName = "^[-_]+".toRegex().replace(fileName, "")

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.renderTemplate(
                e.project!!,
                SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE,
                dialogResult.toMap()
            ),
            "$fileName.md",
            PlainTextFileType.INSTANCE
        )
    }
}
