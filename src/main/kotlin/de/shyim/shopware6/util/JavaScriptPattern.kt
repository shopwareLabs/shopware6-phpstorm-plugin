package de.shyim.shopware6.util

import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.JavascriptLanguage
import com.intellij.lang.javascript.psi.*
import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace

object JavaScriptPattern {
    fun getFeatureIsActive(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .afterSiblingSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                        PlatformPatterns.psiElement().withText("(")
                    )
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java)
                            .withParent(
                                PlatformPatterns.psiElement(JSCallExpression::class.java).withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(
                                            PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                .withFirstChild(
                                                    PlatformPatterns.psiElement().withText("Feature")
                                                )
                                        )
                                        .withLastChild(PlatformPatterns.psiElement().withText("isActive"))
                                )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    fun getComponentPattern(): ElementPattern<PsiElement> {
        return PlatformPatterns.or(
            getComponentExtend(),
            getModuleRouteComponent()
        )
    }

    fun getComponentExtend(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .afterSiblingSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                        PlatformPatterns.psiElement().withText(",")
                            .afterSibling(
                                PlatformPatterns.psiElement(JSLiteralExpression::class.java).afterSibling(
                                    PlatformPatterns.psiElement().withText("(")
                                )
                            )
                    )
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java)
                            .withParent(
                                PlatformPatterns.psiElement(JSCallExpression::class.java).withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(
                                            PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                .withFirstChild(
                                                    PlatformPatterns.psiElement().withText("Component")
                                                )
                                        )
                                        .withLastChild(PlatformPatterns.psiElement().withText("extend"))
                                )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    fun getModuleRouteComponent(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java)
                            .withName("component")
                            .withParent(
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(
                                        PlatformPatterns.psiElement(JSProperty::class.java)
                                            .withParent(
                                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                                    .withParent(
                                                        PlatformPatterns.or(
                                                            PlatformPatterns.psiElement(JSProperty::class.java)
                                                                .withName("children"),
                                                            PlatformPatterns.psiElement(JSProperty::class.java)
                                                                .withName("routes")
                                                        )
                                                    )
                                            )
                                    )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    fun getTranslationPattern(): ElementPattern<PsiElement> {
        return PlatformPatterns.or(
            this.getTcPattern(),
            this.getModuleSnippetPattern(),
            this.getModuleNavigationSnippetPattern()
        )
    }

    private fun getTcPattern(): ElementPattern<out PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .afterSiblingSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                        PlatformPatterns.psiElement().withText("(")
                    )
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java).withParent(
                            PlatformPatterns.psiElement(JSCallExpression::class.java)
                                .withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(PlatformPatterns.psiElement(JSThisExpression::class.java))
                                        .withLastChild(
                                            PlatformPatterns.or(
                                                PlatformPatterns.psiElement().withText("\$tc"),
                                                PlatformPatterns.psiElement().withText("\$t")
                                            )
                                        )
                                )
                        )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    private fun getModuleSnippetPattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java)
                            .withName(PlatformPatterns.string().oneOf("title", "description"))
                            .withParent(
                                this.getModuleBodyPattern()
                            )
                    )
            )
    }

    private fun getModuleNavigationSnippetPattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java).withName("label")
                            .withParent(
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(
                                        PlatformPatterns.psiElement(JSArrayLiteralExpression::class.java)
                                            .withParent(
                                                PlatformPatterns.psiElement(JSProperty::class.java)
                                                    .withName("navigation")
                                                    .withParent(
                                                        this.getModuleBodyPattern()
                                                    )
                                            )
                                    )
                            )
                    )
            )
    }

    fun getModuleBodyPattern(): PsiElementPattern.Capture<JSObjectLiteralExpression> {
        return PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
            .withParent(
                PlatformPatterns.psiElement(JSArgumentList::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSCallExpression::class.java)
                            .withFirstChild(
                                PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                    .withFirstChild(
                                        PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                            .withFirstChild(
                                                PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER).withText("Module")
                                            )
                                    )
                                    .withLastChild(
                                        PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER).withText("register")
                                    )
                            )
                    )
            )
    }

    fun getRepositoryFactoryCreatePattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .afterSiblingSkipping(
                        PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                        PlatformPatterns.psiElement().withText("(")
                    )
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java).withParent(
                            PlatformPatterns.psiElement(JSCallExpression::class.java)
                                .withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(
                                            PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                .withFirstChild(PlatformPatterns.psiElement(JSThisExpression::class.java))
                                                .withLastChild(
                                                    PlatformPatterns.psiElement().withText("repositoryFactory"),
                                                )
                                        )
                                        .withLastChild(PlatformPatterns.psiElement().withText("create"))
                                )
                        )
                    )
            )
    }

    fun getMixinGetByName(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .afterSibling(
                        PlatformPatterns.psiElement().withText("(")
                    )
                    .withParent(
                        PlatformPatterns.psiElement(JSArgumentList::class.java)
                            .withParent(
                                PlatformPatterns.psiElement(JSCallExpression::class.java).withFirstChild(
                                    PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                        .withFirstChild(
                                            PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                .withFirstChild(
                                                    PlatformPatterns.psiElement().withText("Mixin")
                                                )
                                        )
                                        .withLastChild(PlatformPatterns.psiElement().withText("getByName"))
                                )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    fun getRouteCompletion(): ElementPattern<PsiElement> {
        return PlatformPatterns.or(
            getModuleMetaParentRoute(),
            getModuleRedirectRoute(),
            getModuleNavigationRoute(),
            getModuleRouterPush()
        )
    }

    private fun getModuleMetaParentRoute(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java)
                            .withName("parentPath")
                            .withParent(
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(PlatformPatterns.psiElement(JSProperty::class.java).withName("meta"))
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    private fun getModuleRedirectRoute(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java)
                            .withName("name")
                            .withParent(
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(
                                        PlatformPatterns.psiElement(JSProperty::class.java).withName("redirect")
                                    )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    private fun getModuleNavigationRoute(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java)
                            .withName("path")
                            .withParent(
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(
                                        PlatformPatterns.psiElement(JSArrayLiteralExpression::class.java)
                                            .withParent(
                                                PlatformPatterns.psiElement(JSProperty::class.java)
                                                    .withName("navigation")
                                            )
                                    )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

    private fun getModuleRouterPush(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement()
            .withParent(
                PlatformPatterns.psiElement(JSLiteralExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(JSProperty::class.java)
                            .withName("name")
                            .withParent(
                                PlatformPatterns.psiElement(JSObjectLiteralExpression::class.java)
                                    .withParent(
                                        PlatformPatterns.psiElement(JSArgumentList::class.java)
                                            .withParent(
                                                PlatformPatterns.psiElement(JSCallExpression::class.java)
                                                    .withFirstChild(
                                                        PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                            .withFirstChild(
                                                                PlatformPatterns.psiElement(JSReferenceExpression::class.java)
                                                                    .withFirstChild(
                                                                        PlatformPatterns.psiElement(
                                                                            JSThisExpression::class.java
                                                                        )
                                                                    )
                                                                    .withLastChild(
                                                                        PlatformPatterns.psiElement(
                                                                            JSTokenTypes.IDENTIFIER
                                                                        ).withText("\$router")
                                                                    )
                                                            )
                                                            .withLastChild(
                                                                PlatformPatterns.psiElement(JSTokenTypes.IDENTIFIER)
                                                                    .withText("push")
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            )
            .withLanguage(JavascriptLanguage.INSTANCE)
    }

}