package de.shyim.shopware6.hints

import com.intellij.codeInsight.hints.*
import com.intellij.openapi.editor.Editor
import com.intellij.patterns.PlatformPatterns
import com.intellij.patterns.PsiElementPattern
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.php.PhpClassHierarchyUtils
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.parser.PhpElementTypes
import com.jetbrains.php.lang.psi.elements.ArrayCreationExpression
import com.jetbrains.php.lang.psi.elements.PhpClass
import com.jetbrains.php.lang.psi.elements.impl.MethodImpl
import javax.swing.JPanel

class PHPUnitInlineParameterHints : InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings>
        get() = SettingsKey("de.shyim.phpunit.provider")
    override val name: String
        get() = "PHPUnit provider"
    override val previewText: String?
        get() = "Test"

    override fun createSettings() = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector {
        return object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (yieldPattern().accepts(element)) {
                    val fieldName = getFieldName(element, getElementOffsetInYield(element))

                    if (fieldName != null) {
                        val typeRepresentation =
                            factory.smallTextWithoutBackground(fieldName).run { factory.roundWithBackground(this) }
                        sink.addInlineElement(element.textOffset, false, typeRepresentation, false)

                    }
                }

                return true
            }

            private fun getElementOffsetInYield(element: PsiElement): Int {
                element.parent.children.forEachIndexed { index, psiElement ->
                    if (psiElement == element) {
                        return index
                    }
                }

                return 0
            }

            private fun getFieldName(element: PsiElement, parameterOffset: Int): String? {
                if (element.parent.parent.parent.parent.parent !is MethodImpl) {
                    return null
                }

                val providerMethod = (element.parent.parent.parent.parent.parent as MethodImpl)

                val phpClass = providerMethod.parent as PhpClass

                if (!isPhpUnitClass(phpClass)) {
                    return null
                }

                phpClass.ownMethods.forEach {
                    if (it.docComment === null) {
                        return@forEach
                    }

                    val dataProvider = it.docComment!!.getTagElementsByName("@dataProvider")
                    if (dataProvider.isEmpty()) {
                        return@forEach
                    }

                    val providerText = dataProvider.first().text.removePrefix("@dataProvider").trim()

                    if (providerText == providerMethod.name) {
                        return it.getParameter(parameterOffset)?.name
                    }
                }

                return null
            }

            private fun isPhpUnitClass(phpClass: PhpClass): Boolean {
                val phpUnitClass =
                    PhpIndex.getInstance(phpClass.project).getClassesByFQN("\\PHPUnit\\Framework\\TestCase")

                if (phpUnitClass.isEmpty()) {
                    return false
                }

                return PhpClassHierarchyUtils.isSuperClass(
                    phpUnitClass.first(), phpClass, false
                )
            }
        }
    }

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JPanel {
                return JPanel()
            }
        }
    }

    private fun yieldPattern(): PsiElementPattern.Capture<PsiElement> {
        return PlatformPatterns.psiElement(PhpElementTypes.ARRAY_VALUE)
            .withParent(
                PlatformPatterns.psiElement(ArrayCreationExpression::class.java)
                    .withParent(
                        PlatformPatterns.psiElement(PhpElementTypes.YIELD)
                    )
            )
    }
}