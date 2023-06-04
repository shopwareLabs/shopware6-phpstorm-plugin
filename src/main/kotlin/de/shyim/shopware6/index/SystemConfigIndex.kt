package de.shyim.shopware6.index

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.psi.JsonProperty
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.SystemConfig
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer

class SystemConfigIndex : FileBasedIndexExtension<String, SystemConfig>() {
    private val _externalizer = ObjectStreamDataExternalizer<SystemConfig>()

    override fun getName(): ID<String, SystemConfig> {
        return key
    }

    override fun getVersion(): Int {
        return 5
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, SystemConfig, FileContent> {
        return DataIndexer { inputData ->
            if (!isValidFile(inputData)) {
                return@DataIndexer mapOf()
            }

            val namespace = getNamespaceFromInput(inputData) ?: return@DataIndexer mapOf()
            val configs = HashMap<String, SystemConfig>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is XmlTag && (element.name == "input-field" || element.name == "component")) {
                        var name = ""
                        var label = ""


                        element.children.forEach {
                            if (it is XmlTag && it.name == "name") {
                                name = it.value.text
                            } else if (it is XmlTag && it.name == "label" && label.isEmpty()) {
                                label = it.value.text
                            }
                        }

                        configs[name] = SystemConfig(namespace, name, label, inputData.file.path)
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer configs
        }
    }

    private fun getNamespaceFromInput(inputData: FileContent): String? {
        val project = inputData.project
        var parent = inputData.file.parent

        while (parent.findChild("composer.json") == null && parent.findChild("manifest.xml") == null) {
            if (parent.parent == null) {
                return null
            }

            parent = parent.parent
        }

        val composerJson = parent.findChild("composer.json")
        val manifestXml = parent.findChild("manifest.xml")

        if (composerJson != null) {
            val psi = PsiManager.getInstance(project).findFile(composerJson) ?: return null
            var packageName = ""
            var pluginClass = ""

            psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is JsonProperty && element.name == "name" && element.lastChild is JsonStringLiteral) {
                        packageName = element.lastChild.lastChild.text.replace("\"", "")
                    } else if (element is JsonProperty && element.name == "shopware-plugin-class" && element.lastChild is JsonStringLiteral) {
                        pluginClass = element.lastChild.lastChild.text.replace("\"", "")
                    }

                    super.visitElement(element)
                }
            })

            if (packageName.isEmpty()) {
                return null
            }

            if (pluginClass.isEmpty()) { // Core case
                return packageName.split("/")[1] + "." + inputData.fileName.replace(".xml", "")
            }

            val pluginSplit = pluginClass.split("\\")

            return pluginSplit[pluginSplit.size - 1] + ".config"
        }

        if (manifestXml != null) {
            val psi = PsiManager.getInstance(project).findFile(manifestXml) ?: return null
            var namespace = ""

            psi.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is XmlTag && element.name == "name") {
                        namespace = element.value.text
                    }

                    super.visitElement(element)
                }
            })

            if (namespace.isEmpty()) {
                return null
            }

            return namespace
        }

        return null
    }

    private fun isValidFile(file: FileContent): Boolean {
        return String(file.content).contains("System/SystemConfig/Schema/config.xsd")
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<SystemConfig> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, SystemConfig>("de.shyim.shopware6.backend.system-config")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE) {
        }
    }
}
