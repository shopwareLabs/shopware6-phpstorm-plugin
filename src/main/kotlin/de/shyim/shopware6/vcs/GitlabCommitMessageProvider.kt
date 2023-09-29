package de.shyim.shopware6.vcs

import com.intellij.dvcs.DvcsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.LocalChangeList
import com.intellij.openapi.vcs.changes.ui.CommitMessageProvider
import git4idea.branch.GitBranchUtil
import org.apache.commons.lang3.StringUtils

class GitlabCommitMessageProvider : CommitMessageProvider {
    override fun getCommitMessage(forChangelist: LocalChangeList, project: Project): String? {
        try {
            val currentBranch = GitBranchUtil.guessWidgetRepository(project, DvcsUtil.getSelectedFile(project))?.currentBranch ?: return null

            if (!currentBranch.name.startsWith("next-")) {
                return null
            }

            val branchParts = currentBranch.name.split("/")

            if (branchParts.count() == 2) {
                return "${branchParts[0].uppercase()} - ${StringUtils.capitalize(branchParts[1]).replace("-", " ")}"
            }

        } catch (e: Exception) {
            println(e)
        }

        return null
    }
}