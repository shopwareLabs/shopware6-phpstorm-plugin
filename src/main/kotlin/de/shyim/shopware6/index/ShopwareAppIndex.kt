package de.shyim.shopware6.index

import com.intellij.ide.highlighter.XmlFileType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.psi.xml.XmlDocument
import com.intellij.psi.xml.XmlTag
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.ShopwareApp
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap
import org.apache.commons.io.FilenameUtils
import java.nio.file.Paths
import kotlin.io.path.pathString

class ShopwareAppIndex : FileBasedIndexExtension<String, ShopwareApp>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<ShopwareApp>()

    override fun getName(): ID<String, ShopwareApp> {
        return ShopwareAppIndex.key
    }

    override fun getVersion(): Int {
        return 3
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }


    override fun getIndexer(): DataIndexer<String, ShopwareApp, FileContent> {
        return DataIndexer { inputData ->
            val filePath = inputData.file.path.lowercase()
            val fileName = inputData.psiFile.name

            // Consider only apps in custom/apps
            if (!filePath.contains("custom/apps")) {
                return@DataIndexer mapOf()
            }

            if (fileName != "manifest.xml") {
                return@DataIndexer mapOf()
            }

            val apps = THashMap<String, ShopwareApp>()

            var name = ""
            val permissions: MutableList<String> = mutableListOf()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is XmlDocument || (element is XmlTag && (element.name == "manifest" || element.name == "meta"))) {
                        super.visitElement(element)
                        return
                    }

                    if (element is XmlTag && element.name == "name") {
                        name = element.value.text
                    }

                    if (element is XmlTag && element.name == "permissions") {
                        super.visitElement(element)
                        return
                    }

                    if (element is XmlTag && element.parent is XmlTag && (element.parent as XmlTag).name == "permissions" && (element.name == "read" || element.name == "create" || element.name == "update" || element.name == "delete")) {
                        permissions.add("${element.value.text}:${element.name}")
                    }
                }
            })

            val appDir = Paths.get(inputData.file.path).parent
            val expectedStorefrontViewFolder =
                FilenameUtils.separatorsToUnix("${appDir}/Resources/views/")

            apps[name] = ShopwareApp(
                name,
                FilenameUtils.separatorsToUnix(appDir.pathString),
                expectedStorefrontViewFolder,
                permissions
            )

            return@DataIndexer apps
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<ShopwareApp> {
        return EXTERNALIZER
    }

    companion object {
        val key = ID.create<String, ShopwareApp>("de.shyim.shopware6.backend.shopware-app")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE) {
        }
    }
}