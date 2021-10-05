package de.shyim.shopware6.action.generator

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.DumbAwareAction
import de.shyim.shopware6.action.generator.ui.NewChangelogDialogWrapper
import de.shyim.shopware6.templates.ShopwareTemplates
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

        val fileName = modifiedDate + "-" + dialogResult.title.lowercase().replace(" ", "-") + ".md"

        ActionUtil.buildFile(
            e,
            e.project!!,
            ShopwareTemplates.applyChangelogTemplate(e.project!!, dialogResult),
            fileName,
            PlainTextFileType.INSTANCE
        )
    }
}
