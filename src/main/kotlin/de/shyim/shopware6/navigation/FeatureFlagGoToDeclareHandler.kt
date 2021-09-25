package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.FeatureFlagIndex
import de.shyim.shopware6.index.dict.FeatureFlag
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.PHPPattern
import de.shyim.shopware6.util.TwigPattern
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl

class FeatureFlagGoToDeclareHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(
        element: PsiElement?,
        offset: Int,
        editor: Editor?
    ): Array<PsiElement>? {
        if (editor === null || editor.project === null || element == null) {
            return null
        }

        val project = editor.project!!

        val psiElements: MutableList<PsiElement> = ArrayList()

        if (
            PHPPattern.isFeatureFlagFunction(element) ||
            TwigPattern.getPrintBlockOrTagFunctionPattern("feature").accepts(element) ||
            JavaScriptPattern.getFeatureIsActive().accepts(element)
        ) {
            val text = element.text.replace("\"", "").replace("'", "")

            val vals = FileBasedIndex.getInstance()
                .getValues(FeatureFlagIndex.key, text, GlobalSearchScope.allScope(project))

            vals.forEach {
                lookupPsiElementFromFile(psiElements, it, project)
            }
        }

        return psiElements.toTypedArray()
    }

    private fun lookupPsiElementFromFile(
        psiElements: MutableList<PsiElement>,
        featureFlag: FeatureFlag,
        project: Project
    ) {
        val file = LocalFileSystem.getInstance().findFileByPath(featureFlag.file)

        if (file != null) {
            val psi = PsiManager.getInstance(project).findFile(file)

            if (psi != null) {
                psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                    override fun visitElement(element: PsiElement) {
                        if (element is YAMLPlainTextImpl) {
                            if (element.text == featureFlag.name) {
                                psiElements.add(element)
                                return
                            }
                        }

                        super.visitElement(element)
                    }
                })
            }
        }
    }
}