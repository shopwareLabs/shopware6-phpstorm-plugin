package de.shyim.shopware6.util

import com.intellij.patterns.ElementPattern
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.twig.TwigLanguage
import com.jetbrains.twig.TwigTokenTypes
import com.jetbrains.twig.elements.TwigElementTypes


object TwigPattern {
    fun getTranslationKeyPattern(vararg type: String?): ElementPattern<PsiElement?> {
        return PlatformPatterns
            .psiElement(TwigTokenTypes.STRING_TEXT)
            .beforeLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                    PlatformPatterns.psiElement(TwigTokenTypes.WHITE_SPACE),
                    PlatformPatterns.psiElement(TwigTokenTypes.SINGLE_QUOTE),
                    PlatformPatterns.psiElement(TwigTokenTypes.DOUBLE_QUOTE)
                ),
                PlatformPatterns.psiElement(TwigTokenTypes.FILTER).beforeLeafSkipping(
                    PlatformPatterns.or(
                        PlatformPatterns.psiElement(TwigTokenTypes.WHITE_SPACE),
                        PlatformPatterns.psiElement(PsiWhiteSpace::class.java)
                    ),
                    PlatformPatterns.psiElement(TwigTokenTypes.IDENTIFIER).withText(
                        PlatformPatterns.string().oneOf(*type)
                    )
                )
            )
            .withLanguage(TwigLanguage.INSTANCE)
    }

    fun getPrintBlockOrTagFunctionPattern(vararg functionName: String?): ElementPattern<PsiElement?> {
        return PlatformPatterns
            .psiElement(TwigTokenTypes.STRING_TEXT)
            .withParent(
                getFunctionCallScopePattern()
            )
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(TwigTokenTypes.LBRACE),
                    PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                    PlatformPatterns.psiElement(TwigTokenTypes.WHITE_SPACE),
                    PlatformPatterns.psiElement(TwigTokenTypes.SINGLE_QUOTE),
                    PlatformPatterns.psiElement(TwigTokenTypes.DOUBLE_QUOTE)
                ),
                PlatformPatterns.psiElement(TwigTokenTypes.IDENTIFIER)
                    .withText(PlatformPatterns.string().oneOf(*functionName))
            )
            .withLanguage(TwigLanguage.INSTANCE)
    }

    private fun getFunctionCallScopePattern(): ElementPattern<PsiElement?> {
        return PlatformPatterns.or( // old and inconsistent implementations of FUNCTION_CALL:
            // eg {% if asset('') %} does not provide a FUNCTION_CALL whereas a print block does
            PlatformPatterns.psiElement(TwigElementTypes.PRINT_BLOCK),
            PlatformPatterns.psiElement(TwigElementTypes.TAG),
            PlatformPatterns.psiElement(TwigElementTypes.IF_TAG),
            PlatformPatterns.psiElement(TwigElementTypes.SET_TAG),
            PlatformPatterns.psiElement(TwigElementTypes.ELSE_TAG),
            PlatformPatterns.psiElement(TwigElementTypes.ELSEIF_TAG),
            PlatformPatterns.psiElement(TwigElementTypes.FOR_TAG),  // PhpStorm 2017.3.2: {{ asset('') }}
            PlatformPatterns.psiElement(TwigElementTypes.FUNCTION_CALL)
        )
    }

    fun getTcPattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns
            .psiElement(TwigTokenTypes.STRING_TEXT)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                    PlatformPatterns.psiElement(TwigTokenTypes.SINGLE_QUOTE),
                    PlatformPatterns.psiElement(TwigTokenTypes.DOUBLE_QUOTE),
                    PlatformPatterns.psiElement(TwigTokenTypes.LBRACE),
                ),
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(TwigTokenTypes.VAR_IDENTIFIER).withText("\$tc"),
                    PlatformPatterns.psiElement(TwigTokenTypes.VAR_IDENTIFIER).withText("\$t")
                )
            )
            .withLanguage(TwigLanguage.INSTANCE)

    }

    fun getShopwareIncludeExtendsTagPattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement(TwigTokenTypes.STRING_TEXT)
            .afterLeafSkipping(
                PlatformPatterns.or(
                    PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                    PlatformPatterns.psiElement(TwigTokenTypes.SINGLE_QUOTE),
                    PlatformPatterns.psiElement(TwigTokenTypes.DOUBLE_QUOTE),
                    PlatformPatterns.psiElement(TwigTokenTypes.LBRACE)
                ),
                PlatformPatterns.psiElement(TwigTokenTypes.IDENTIFIER).withText("search")
            )
            .withParent(
                PlatformPatterns.psiElement(TwigElementTypes.METHOD_CALL)
                    .withFirstChild(
                        PlatformPatterns.psiElement(TwigElementTypes.FIELD_REFERENCE)
                            .withFirstChild(
                                PlatformPatterns.psiElement(TwigElementTypes.VARIABLE_REFERENCE).withText("services")
                                    .beforeLeafSkipping(
                                        PlatformPatterns.or(
                                            PlatformPatterns.psiElement(PsiWhiteSpace::class.java),
                                            PlatformPatterns.psiElement(TwigTokenTypes.DOT)
                                        ),
                                        PlatformPatterns.psiElement(TwigTokenTypes.IDENTIFIER).withText("repository")
                                    )
                            )
                    )
            )
            .withLanguage(TwigLanguage.INSTANCE)
    }

    fun getScriptRepositorySearchPattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement(TwigTokenTypes.STRING_TEXT)
            .withParent(
                PlatformPatterns.psiElement(TwigElementTypes.METHOD_CALL)
                    .withFirstChild(
                        PlatformPatterns.psiElement(TwigElementTypes.FIELD_REFERENCE)
                            .beforeLeafSkipping(
                                PlatformPatterns.or(PlatformPatterns.psiElement(TwigTokenTypes.DOT)),
                                PlatformPatterns.psiElement(TwigTokenTypes.IDENTIFIER).withText("search")
                            )
                            .withFirstChild(
                                PlatformPatterns.psiElement(TwigElementTypes.VARIABLE_REFERENCE).withText("services")
                                    .beforeLeafSkipping(
                                        PlatformPatterns.or(PlatformPatterns.psiElement(TwigTokenTypes.DOT)),
                                        PlatformPatterns.psiElement(TwigTokenTypes.IDENTIFIER).withText("repository")
                                    )
                            )
                    )
            )
            .withLanguage(TwigLanguage.INSTANCE)
    }
}