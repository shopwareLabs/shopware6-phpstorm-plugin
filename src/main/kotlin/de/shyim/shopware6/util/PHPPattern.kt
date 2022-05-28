package de.shyim.shopware6.util

import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.psi.elements.*
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
        MethodMatcher.CallToSignature("\\Shopware\\Core\\Framework\\Feature", "triggerDeprecationOrThrow"),
    )

    private val SHOPWARE_STOREFRONT_CONTROLLER_TRANS_SIGNATURES: Array<MethodMatcher.CallToSignature> = arrayOf(
        MethodMatcher.CallToSignature("\\Shopware\\Storefront\\Controller\\StorefrontController", "trans")
    )

    private val SHOPWARE_CORE_SYSTEM_CONFIG_SERVICE_GET_SINGLE: Array<MethodMatcher.CallToSignature> = arrayOf(
        MethodMatcher.CallToSignature("\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService", "get"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService", "getString"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService", "getInt"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService", "getFloat"),
        MethodMatcher.CallToSignature("\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService", "getBool"),
    )

    private val SHOPWARE_CORE_SYSTEM_CONFIG_SERVICE_GET_DOMAIN: Array<MethodMatcher.CallToSignature> = arrayOf(
        MethodMatcher.CallToSignature("\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService", "getDomain"),
    )

    private val SHOPWARE_CORE_CRITERIA_ADD_FIELDS: Array<MethodMatcher.CallToSignature> = arrayOf(
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addAssociation"
        ),
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addAssociations"
        ),
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addFilter"
        ),
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addPostFilter"
        ),
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addSorting"
        ),
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addGroupField"
        ),
    )

    private val SHOPWARE_CORE_CRITERIA_ADD_AGGREGATION: Array<MethodMatcher.CallToSignature> = arrayOf(
        MethodMatcher.CallToSignature(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Search\\Criteria",
            "addAggregation"
        ),
    )

    fun isFeatureFlagFunction(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(element.context, FEATURE_FLAG_SIGNATURES) != null
    }

    fun isShopwareStorefrontControllerTrans(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(
            element.context,
            SHOPWARE_STOREFRONT_CONTROLLER_TRANS_SIGNATURES
        ) != null
    }

    fun isShopwareCoreSystemConfigServiceGetSingle(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(
            element.context,
            SHOPWARE_CORE_SYSTEM_CONFIG_SERVICE_GET_SINGLE
        ) != null
    }

    fun isShopwareCoreSystemConfigServiceGetDomain(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(
            element.context,
            SHOPWARE_CORE_SYSTEM_CONFIG_SERVICE_GET_DOMAIN
        ) != null
    }

    fun isShopwareCriteriaAddFields(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(
            element.context,
            SHOPWARE_CORE_CRITERIA_ADD_FIELDS
        ) != null
    }

    fun isShopwareCriteriaAddAggregation(element: PsiElement): Boolean {
        return MethodMatcher.getMatchedSignatureWithDepth(
            element.context,
            SHOPWARE_CORE_CRITERIA_ADD_AGGREGATION
        ) != null
    }

    fun isCriteriaPatternAddAssociation(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java).inside(
                PlatformPatterns.psiElement(ParameterList::class.java)
            )
        )
    }

    fun isCriteriaPatternAddAssociations(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java).withParent(
                PlatformPatterns.psiElement(PhpPsiElement::class.java)
                    .inside(
                        PlatformPatterns.psiElement(ArrayCreationExpression::class.java)
                            .withParent(
                                PlatformPatterns.psiElement(ParameterList::class.java)
                            )
                    )
            )
        )
    }

    fun isCriteriaPatternAddFilter(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .insideStarting(
                    PlatformPatterns.psiElement(ParameterList::class.java)
                        .withParent(
                            PlatformPatterns.psiElement(NewExpression::class.java)
                                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                        )
                )
        )
    }

    fun isCriteriaPatternAddAggregation(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement().withParent(
            PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                .afterSibling(
                    PlatformPatterns.psiElement(StringLiteralExpression::class.java)
                )
                .inside(
                    PlatformPatterns.psiElement(ParameterList::class.java)
                        .withParent(
                            PlatformPatterns.psiElement(NewExpression::class.java)
                                .withParent(PlatformPatterns.psiElement(ParameterList::class.java))
                        )
                )
        )
    }
}