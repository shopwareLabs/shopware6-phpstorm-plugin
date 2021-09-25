package de.shyim.shopware6.util

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
}