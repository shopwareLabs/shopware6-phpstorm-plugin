package de.shyim.shopware6.inspection.quickfix.twig

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.util.TwigUtil

class AddMissingTwigVersioningCommentFix : LocalQuickFix {
    override fun getFamilyName() = "Add missing versioning comment"

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        descriptor.psiElement?.containingFile?.virtualFile?.path?.let {
            TwigUtil.addVersioningComment(
                descriptor.psiElement.children[0] as TwigBlockTag,
                TwigUtil.getRelativePath(it)
            )
        }
    }
}