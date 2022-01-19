package de.shyim.shopware6.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.FeatureFlag
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap
import org.jetbrains.yaml.YAMLFileType
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.impl.YAMLSequenceItemImpl

class FeatureFlagIndex : FileBasedIndexExtension<String, FeatureFlag>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<FeatureFlag>()

    override fun getName(): ID<String, FeatureFlag> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, FeatureFlag, FileContent> {
        return DataIndexer { inputData ->
            if (inputData.file.name != "shopware.yaml" && inputData.file.name != "feature.yaml") {
                return@DataIndexer mapOf()
            }

            if (inputData.file.parent.name != "packages") {
                return@DataIndexer mapOf()
            }

            val flags = THashMap<String, FeatureFlag>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is YAMLKeyValue) {
                        if (element.firstChild.text != "shopware" && element.firstChild.text != "feature" && element.firstChild.text != "flags") {
                            return
                        }

                        if (element.firstChild.text == "flags") {
                            element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                override fun visitElement(element: PsiElement) {
                                    if (element is YAMLSequenceItemImpl) {
                                        val map = HashMap<String, String>()

                                        element.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                                            override fun visitElement(element: PsiElement) {
                                                if (element is YAMLKeyValue) {
                                                    map.set(element.firstChild.text, element.lastChild.firstChild.text)
                                                }

                                                super.visitElement(element)
                                            }
                                        })

                                        if (map.containsKey("name")) {
                                            var default = false
                                            var major = false

                                            if (map.containsKey("default") && map.get("default") == "true") {
                                                default = true
                                            }

                                            if (map.containsKey("major") && map.get("major") == "true") {
                                                major = true
                                            }

                                            flags[map.get("name")] = FeatureFlag(
                                                map.getOrDefault("name", ""),
                                                default,
                                                major,
                                                map.getOrDefault("description", ""),
                                                inputData.file.path
                                            )
                                        }

                                        return
                                    }

                                    super.visitElement(element)
                                }
                            })

                            return
                        }
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer flags
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<FeatureFlag> {
        return EXTERNALIZER
    }

    companion object {
        val key = ID.create<String, FeatureFlag>("de.shyim.shopware6.core.feature_flag")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(YAMLFileType.YML) {
        }
    }
}