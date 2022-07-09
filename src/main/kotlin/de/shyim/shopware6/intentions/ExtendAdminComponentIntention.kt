package de.shyim.shopware6.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import de.shyim.shopware6.action.context.admin.ExtendAdminComponentAction
import de.shyim.shopware6.util.JavaScriptPattern

class ExtendAdminComponentIntention: PsiElementBaseIntentionAction() {
    override fun getFamilyName() = "Extend/override this component"
    override fun getText() = "Extend/override this component"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return JavaScriptPattern.getComponentRegisterFirstParameter().accepts(element)
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        if (editor == null || editor !is EditorImpl) {
            return
        }

        ExtendAdminComponentAction.createComponent(element, editor)
    }

    override fun checkFile(file: PsiFile?): Boolean {
        return true
    }
}