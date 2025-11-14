package de.shyim.shopware6.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import de.shyim.shopware6.action.context.admin.ExtendAdminComponentMethodAction
import icons.ShopwareToolBoxIcons
import javax.swing.Icon

class ExtendAdminComponentMethodIntention : PsiElementBaseIntentionAction(), Iconable {
    override fun getFamilyName() = "Override this method"
    override fun getText() = "Override this method"

    override fun getIcon(p0: Int): Icon {
        return ShopwareToolBoxIcons.SHOPWARE
    }

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return ExtendAdminComponentMethodAction.getPattern().accepts(element)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null || editor !is EditorImpl) {
            return
        }

        ExtendAdminComponentMethodAction.extendMethod(element, editor)
    }

    override fun checkFile(file: PsiFile?): Boolean {
        return true
    }
}