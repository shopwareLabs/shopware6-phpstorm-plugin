package de.shyim.shopware6.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.twig.TwigFileType
import de.shyim.shopware6.util.TwigUtil

/**
 * Indexes every template by its path relative to Resources/views. The value is the template
 * reference of the sw_extends tag inside the file (empty when the file does not extend).
 */
class ShopwareTemplateIndex : FileBasedIndexExtension<String, String>() {
    override fun getName(): ID<String, String> {
        return key
    }

    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return DataIndexer { inputData ->
            if (!inputData.file.path.contains("Resources/views/")) {
                return@DataIndexer mapOf()
            }

            mapOf(
                TwigUtil.getRelativePath(inputData.file.path) to
                        (TwigUtil.findExtendsTargetReference(inputData.contentAsText) ?: "")
            )
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): EnumeratorStringDescriptor {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(TwigFileType.INSTANCE) {
        }
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    companion object {
        val key = ID.create<String, String>("de.shyim.shopware6.frontend.twig_templates")
    }
}
