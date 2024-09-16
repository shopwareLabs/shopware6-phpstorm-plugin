package de.shyim.shopware6.marker.js

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.lang.javascript.psi.JSArgumentList
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import de.shyim.shopware6.index.AdminComponentOverrideIndex
import de.shyim.shopware6.util.JavaScriptPattern
import de.shyim.shopware6.util.StringUtil
import icons.ShopwareToolBoxIcons

class AdminComponentMarker : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (getComponentRegister().accepts(element)) {
            val targets = mutableListOf<PsiElement>()

            val targetComponent = StringUtil.stripQuotes(element.text)
            val overrides = FileBasedIndex.getInstance().getValues(
                AdminComponentOverrideIndex.key,
                targetComponent, GlobalSearchScope.projectScope(element.project)
            )

            overrides.forEach {
                val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                if (file != null) {
                    val psi = PsiManager.getInstance(element.project).findFile(file)

                    if (psi != null) {
                        targets.add(psi)
                    }
                }
            }

            for (key in FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, element.project)) {
                val values = FileBasedIndex.getInstance()
                    .getValues(AdminComponentIndex.key, key, GlobalSearchScope.allScope(element.project))

                values.forEach {
                    if (it.extends == targetComponent) {
                        val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                        if (file != null) {
                            val psi = PsiManager.getInstance(element.project).findFile(file)

                            if (psi != null) {
                                targets.add(psi)
                            }
                        }
                    }
                }
            }

            if (targets.isNotEmpty()) {
                val builder: NavigationGutterIconBuilder<PsiElement> =
                    NavigationGutterIconBuilder.create(ShopwareToolBoxIcons.SHOPWARE)
                        .setTargets(targets)
                        .setTooltipText("Overrides/Extends")
                result.add(builder.createLineMarkerInfo(element))
            }
        }

        if (getComponentOverride().accepts(element) || JavaScriptPattern.getComponentExtend().accepts(element)) {
            val targets = mutableListOf<PsiElement>()

            val targetComponent = StringUtil.stripQuotes(element.text)

            val values = FileBasedIndex.getInstance()
                .getValues(AdminComponentIndex.key, targetComponent, GlobalSearchScope.allScope(element.project))

            values.forEach {
                val file = LocalFileSystem.getInstance().findFileByPath(it.file)

                if (file != null) {
                    val psi = PsiManager.getInstance(element.project).findFile(file)

                    if (psi != null) {
                        targets.add(psi)
                    }
                }
            }

            if (targets.isNotEmpty()) {
                val builder: NavigationGutterIconBuilder<PsiElement> =
                    NavigationGutterIconBuilder.create(ShopwareToolBoxIcons.SHOPWARE)
                        .setTargets(targets)
                        .setTooltipText("Base component")
                result.add(builder.createLineMarkerInfo(element))
            }
        }
    }

    private fun getComponentRegister(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java)
                            .withParent(
                                PlatformPatterns.psiElement(JSCallExpression::class.java)
                                    .withFirstChild(
                                        PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                            .withFirstChild(
                                                PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                    .withFirstChild(
                                                        PlatformPatterns.psiElement().withText("Component")
                                                    )
                                            )
                                            .withLastChild(PlatformPatterns.psiElement().withText("register"))
                                    )
                            )
                    )
            )
    }

    private fun getComponentOverride(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java)
                            .withParent(
                                PlatformPatterns.psiElement(JSCallExpression::class.java)
                                    .withFirstChild(
                                        PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                            .withFirstChild(
                                                PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                    .withFirstChild(
                                                        PlatformPatterns.psiElement().withText("Component")
                                                    )
                                            )
                                            .withLastChild(PlatformPatterns.psiElement().withText("override"))
                                    )
                            )
                    )
            )
    }
}