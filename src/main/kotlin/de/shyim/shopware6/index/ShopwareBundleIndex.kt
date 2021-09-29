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
import java.nio.file.Paths
import kotlin.io.path.Path

class ShopwareBundleIndex: FileBasedIndexExtension<String, ShopwareBundle>() {
    private val EXTERNALIZER = ObjectStreamDataExternalizer<ShopwareBundle>()

    override fun getName(): ID<String, ShopwareBundle> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, ShopwareBundle, FileContent> {
        return DataIndexer { inputData ->
            val bundles = THashMap<String, ShopwareBundle>()

            inputData.psiFile.acceptChildren(object: PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    if (element is PhpClass && !element.isAbstract && isShopwareBundle(element)) {
                        bundles[element.name] = ShopwareBundle(element.name, inputData.file.path, getViewDirectory(inputData.file.path))
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer bundles
        }
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

    fun getViewDirectory(folderPath: String): String? {
        val expectedViewDir =
            FilenameUtils.separatorsToUnix("${Paths.get(folderPath).parent}/Resources/views/storefront/")

        if (Files.exists(Path(expectedViewDir))) {
            return expectedViewDir
        }

        return null
    }
}