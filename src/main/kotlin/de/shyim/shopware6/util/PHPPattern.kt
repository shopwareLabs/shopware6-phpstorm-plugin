package de.shyim.shopware6.util

import com.intellij.psi.PsiElement
import fr.adrienbrault.idea.symfony2plugin.util.MethodMatcher

object PHPPattern {
    private val FEATURE_FLAG_SIGNATURES: Array<MethodMatcher.CallToSignature> = arrayOf(
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "isActive"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "ifActive"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "ifActiveCall"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "skipTestIfInActive"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "skipTestIfActive"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "triggerDeprecated"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "throwException"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "has"),
    )

    fun isFeatureFlagFunction(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(element.getContext(), FEATURE_FLAG_SIGNATURES) != null
    }
}