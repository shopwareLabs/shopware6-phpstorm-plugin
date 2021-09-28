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

    fun getTranslationPattern(): ElementPattern<PsiElement> {
        return PlatformPatterns.or(
            this.getTcPattern(),
            this.getModuleSnippetPattern(),
            this.getModuleNavigationSnippetPattern()
        )
    }

    fun getTcPattern(): ElementPattern<out PsiElement> {
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

    fun getModuleSnippetPattern(): PsiElementPattern.Capture<PsiElement> {
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

    fun getModuleNavigationSnippetPattern(): PsiElementPattern.Capture<PsiElement> {
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
}