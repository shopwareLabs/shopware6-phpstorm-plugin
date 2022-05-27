package de.shyim.shopware6.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.elements.PhpClass
import de.shyim.shopware6.index.dict.ShopwareBundle
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import gnu.trove.THashMap
import org.apache.commons.io.FilenameUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.pathString

class ShopwareBundleIndex: FileBasedIndexExtension<String, ShopwareBundle>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<ShopwareBundle>()

    override fun getName(): ID<String, ShopwareBundle> {
        return key
    }

    override fun getVersion(): Int {
        return 4
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, ShopwareBundle, FileContent> {
        return DataIndexer { inputData ->
            if (!isValidForIndex(inputData)) {
                return@DataIndexer mapOf()
            }

            val bundles = THashMap<String, ShopwareBundle>()

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is PhpClass && !element.isAbstract && isShopwareBundle(element)) {
                        if (element.name.isEmpty()) {
                            return
                        }

                        val bundleDir = Paths.get(inputData.file.path).parent
                        val expectedStorefrontViewFolder =
                            FilenameUtils.separatorsToUnix("${bundleDir}/Resources/views/")
                        bundles[element.name] =
                            ShopwareBundle(
                                element.name,
                                inputData.file.path,
                                expectedStorefrontViewFolder,
                                getRootFolder(bundleDir)
                            )
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer bundles
        }
    }

    private fun getRootFolder(bundleDir: Path): String {
        if (Files.exists(Paths.get("${bundleDir}/composer.json"))) {
            return FilenameUtils.separatorsToUnix(bundleDir.pathString)
        }

        return getRootFolder(bundleDir.parent)
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<ShopwareBundle> {
        return EXTERNALIZER
    }

    companion object {
        val key = ID.create<String, ShopwareBundle>("de.shyim.shopware6.backend.shopware-bundles")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(PhpFileType.INSTANCE) {
        }
    }

    fun isShopwareBundle(pClass: PhpClass): Boolean {
        if (pClass.extendsList.referenceElements.size == 0) {
            return false
        }

        var found = false

        pClass.extendsList.referenceElements.forEach {
            if (it.fqn == "\\Shopware\\Core\\Framework\\Bundle" || it.fqn == "\\Shopware\\Core\\Framework\\Plugin") {
                found = true
            }
        }

        return found
    }

    fun isValidForIndex(inputData: FileContent): Boolean {
        val fileName = inputData.psiFile.name
        val filePath = inputData.file.path.lowercase()

        if (fileName.startsWith(".") || fileName.endsWith("Test")) {
            return false
        }

        if (
            filePath.contains("core/framework/test/plugin/_fixture/plugins/") ||
            filePath.contains("core/framework/test/store/_fixtures") ||
            filePath.contains("core/content/test/") ||
            filePath.contains("core/system/test/snippet/files/_fixtures") ||
            filePath.contains("core/system/test/systemconfig/_fixtures/") ||
            filePath.contains("core/framework/test/plugin/requirement/_fixture/") ||
            filePath.contains("core/framework/test/adapter/twig/fixtures") ||
            filePath.contains("storefront/test/theme/fixtures/") ||
            filePath.contains("core/framework/test/api/controller/fixtures")
        ) {
            return false
        }

        return true
    }
}