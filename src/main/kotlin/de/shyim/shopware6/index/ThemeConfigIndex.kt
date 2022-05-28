package de.shyim.shopware6.index

import com.intellij.json.JsonFileType
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.ThemeConfig
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap

class ThemeConfigIndex : FileBasedIndexExtension<String, ThemeConfig>() {
    private val _externalizer = ObjectStreamDataExternalizer<ThemeConfig>()

    override fun getIndexer(): DataIndexer<String, ThemeConfig, FileContent> {
        return DataIndexer { inputData ->
            if (inputData.file.name != "theme.json" || inputData.file.parent.name != "Resources") {
                return@DataIndexer mapOf()
            }

            val configs = THashMap<String, ThemeConfig>()

            inputData.psiFile.accept(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {

                    if (element is JsonProperty) {
                        if (element.firstChild.text != "\"config\"") {
                            return
                        }

                        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                            override fun visitElement(element: PsiElement) {
                                if (element is JsonProperty) {
                                    if (element.firstChild.text != "\"fields\"") {
                                        return
                                    }


                                    if (element.lastChild is JsonObject) {
                                        element.lastChild.children.forEach { fieldElement ->
                                            if (fieldElement is JsonProperty) {
                                                val name = fieldElement.firstChild.text.replace("\"", "")
                                                var label = ""
                                                var value = ""

                                                if (fieldElement.lastChild is JsonObject) {
                                                    fieldElement.lastChild.children.forEach { attributeElement ->
                                                        if (attributeElement is JsonProperty) {
                                                            if (attributeElement.firstChild.text == "\"label\"" && attributeElement.lastChild is JsonObject) {
                                                                attributeElement.lastChild.children.forEach { labelTranslationElement ->
                                                                    if (labelTranslationElement is JsonProperty && label.isEmpty()) {
                                                                        if (labelTranslationElement.lastChild is JsonStringLiteral) {
                                                                            label =
                                                                                labelTranslationElement.lastChild.text.replace(
                                                                                    "\"",
                                                                                    ""
                                                                                )
                                                                        }
                                                                    }
                                                                }
                                                            } else if (attributeElement.firstChild.text == "\"value\"" && attributeElement.lastChild is JsonStringLiteral) {
                                                                value =
                                                                    attributeElement.lastChild.text.replace("\"", "")
                                                            }
                                                        }
                                                    }
                                                }

                                                configs[name] = ThemeConfig(name, label, value, inputData.file.path)
                                            }
                                        }
                                    }
                                }

                                super.visitElement(element)
                            }
                        })

                        return
                    }

                    super.visitElement(element)
                }
            })


            return@DataIndexer configs
        }
    }

    override fun getName(): ID<String, ThemeConfig> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<ThemeConfig> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, ThemeConfig>("de.shyim.shopware6.frontend.theme_config")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {
        }
    }
}