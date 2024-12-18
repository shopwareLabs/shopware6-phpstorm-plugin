package de.shyim.shopware6.navigation

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.navigation.ItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.psi.PsiElement
import javax.swing.Icon

class NavigationItemEx : NavigationItem, ItemPresentation {
    private val psiElement: PsiElement
    private val name: String
    private val icon: Icon
    private val locationString: String
    private var appendBundleLocation: Boolean = true

    constructor(
        psiElement: PsiElement,
        name: String,
        icon: Icon,
        locationString: String
    ) {
        this.psiElement = psiElement
        this.name = name
        this.icon = icon
        this.locationString = locationString
    }

    constructor(
        psiElement: PsiElement,
        name: String,
        icon: Icon,
        locationString: String,
        appendBundleLocation: Boolean
    ) : this(psiElement, name, icon, locationString) {
        this.appendBundleLocation = appendBundleLocation
    }

    override fun getName(): String? = name

    override fun getPresentation(): ItemPresentation? = this

    override fun navigate(requestFocus: Boolean) {
        val descriptor = PsiNavigationSupport.getInstance().getDescriptor(psiElement)
        descriptor?.navigate(requestFocus)
    }

    override fun canNavigate(): Boolean {
        return PsiNavigationSupport.getInstance().canNavigate(psiElement)
    }

    override fun canNavigateToSource(): Boolean = canNavigate()

    override fun toString(): String = name

    override fun getPresentableText(): String? = name

    override fun getLocationString(): String? {
        if (!appendBundleLocation) {
            return locationString
        }

        val psiFile = psiElement.containingFile ?: return locationString

        val locationPathString = locationString
        val bundleName = psiFile.virtualFile.path

        if (bundleName.contains("Bundle")) {
            val bundleSubstring = bundleName.substring(0, bundleName.lastIndexOf("Bundle"))
            if (bundleSubstring.length > 1 && bundleSubstring.contains("/")) {
                return "$locationPathString ${bundleSubstring.substring(bundleSubstring.lastIndexOf("/") + 1)}::${psiFile.name}"
            }
        }

        return "$locationPathString ${psiFile.name}"
    }

    override fun getIcon(unused: Boolean): Icon? = icon
}