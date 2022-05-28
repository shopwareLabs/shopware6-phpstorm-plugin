package de.shyim.shopware6.symfonyplugin

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.twig.TwigTokenTypes
import com.jetbrains.twig.elements.TwigElementTypes
import fr.adrienbrault.idea.symfony2plugin.extension.TwigFileUsage

class TwigFileUsageProvider : TwigFileUsage {
    override fun getExtendsTemplate(element: PsiElement): MutableCollection<String> {
        return getTemplateName(element)
    }

    override fun getIncludeTemplate(element: PsiElement): MutableCollection<String> {
        return getTemplateName(element)
    }

    override fun isExtendsTemplate(element: PsiElement): Boolean {
        return element.elementType == TwigElementTypes.TAG && element.node.findChildByType(TwigTokenTypes.TAG_NAME)?.text == "sw_extends"
    }

    override fun isIncludeTemplate(element: PsiElement): Boolean {
        return element.elementType == TwigElementTypes.TAG && element.node.findChildByType(TwigTokenTypes.TAG_NAME)?.text == "sw_include"
    }

    private fun getTemplateName(element: PsiElement): MutableCollection<String> {
        element.node.getChildren(null).forEach {
            if (it.elementType == TwigTokenTypes.STRING_TEXT) {
                return mutableListOf(it.text)
            }
        }

        return mutableListOf()
    }
}