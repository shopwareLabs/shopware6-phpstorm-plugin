package de.shyim.shopware6.util

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiElement
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.elements.impl.ClassReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.FieldReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.MethodReferenceImpl
import com.jetbrains.php.lang.psi.elements.impl.PhpClassImpl
import com.jetbrains.php.lang.psi.elements.impl.StringLiteralExpressionImpl
import com.jetbrains.php.lang.psi.elements.impl.VariableImpl

object PHPPattern {
    fun isStaticClassMethodCall(element: PsiElement): Boolean {
        return PlatformPatterns
            .psiElement()
            .withParent(
                PlatformPatterns
                    .psiElement(StringLiteralExpressionImpl::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(PhpElementTypes.PARAMETER_LIST)
                            .withParent(
                                PlatformPatterns
                                    .psiElement(PhpElementTypes.METHOD_REFERENCE)
                            )
                    )

            ).accepts(element)
    }

    fun isFeatureFlagFunction(element: PsiElement): Boolean {
        if (!isStaticClassMethodCall(element)) {
            return false
        }

        val methodReference = element.parent.parent.parent as MethodReferenceImpl

        if (methodReference.classReference !is ClassReferenceImpl) {
            return false
        }

        val classRef = methodReference.classReference as ClassReferenceImpl

        if (classRef.fqn != "\\Shopware\\Core\\Framework\\Feature") {
            return false
        }

        return methodReference.name == "isActive" || methodReference.name == "ifActive" || methodReference.name == "ifActiveCall" || methodReference.name == "skipTestIfInActive" || methodReference.name == "skipTestIfActive" || methodReference.name == "triggerDeprecated" || methodReference.name == "throwException" || methodReference.name == "has" || methodReference.name == "triggerDeprecationOrThrow"
    }

    fun isShopwareStorefrontControllerTrans(element: PsiElement): Boolean {
        if (!isStaticClassMethodCall(element)) {
            return false
        }

        val methodReference = element.parent.parent.parent as MethodReferenceImpl

        if (methodReference.name != "trans") {
            return false
        }

        if (methodReference.classReference !is VariableImpl) {
            return false
        }

        val resolved = (methodReference.classReference as VariableImpl).resolve()

        if (resolved == null) {
            return false
        }

        val classContaining = resolved as PhpClassImpl

        classContaining.extendsList.children.forEach {
            if ((it as ClassReferenceImpl).fqn == "\\Shopware\\Storefront\\Controller\\StorefrontController") {
                return true
            }
        }

        return false
    }

    fun isShopwareCoreSystemConfigServiceGetSingle(element: PsiElement): Boolean {
        if (!isStaticClassMethodCall(element)) {
            return false
        }

        val methodReference = element.parent.parent.parent as MethodReferenceImpl

        if (methodReference.classReference !is FieldReferenceImpl) {
            return false
        }

        val fieldRef = methodReference.classReference as FieldReferenceImpl


        if (fieldRef.resolveLocalType().toString() != "\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService") {
            return false
        }

        return methodReference.name == "get" || methodReference.name == "getString" || methodReference.name == "getInt" || methodReference.name == "getFloat" || methodReference.name == "getBool"
    }

    fun isShopwareCoreSystemConfigServiceGetDomain(element: PsiElement): Boolean {
        if (!isStaticClassMethodCall(element)) {
            return false
        }

        val methodReference = element.parent.parent.parent as MethodReferenceImpl

        if (methodReference.classReference !is FieldReferenceImpl) {
            return false
        }

        val fieldRef = methodReference.classReference as FieldReferenceImpl


        if (fieldRef.resolveLocalType().toString() != "\\Shopware\\Core\\System\\SystemConfig\\SystemConfigService") {
            return false
        }

        return methodReference.name == "getDomain"
    }
}