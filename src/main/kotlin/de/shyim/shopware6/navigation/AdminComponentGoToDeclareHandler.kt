package de.shyim.shopware6.navigation

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.html.HtmlTagImpl
import com.intellij.psi.impl.source.xml.XmlTokenImpl
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.util.JavaScriptPattern

class AdminComponentGoToDeclareHandler : GotoDeclarationHandler {
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

        if (JavaScriptPattern.getComponentExtend()
                .accepts(element) || (element is XmlTokenImpl && element.parent is HtmlTagImpl)
        ) {
            val text = element.text.replace("\"", "").replace("'", "")

            val values = FileBasedIndex.getInstance()
                .getValues(AdminComponentIndex.key, text, GlobalSearchScope.allScope(project))

            values.forEach {
                val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                if (file != null) {
                    val psi = PsiManager.getInstance(project).findFile(file)

                    if (psi != null) {
                        psiElements.add(psi)
                    }
                }
            }
        }

        return psiElements.toTypedArray()
    }
}