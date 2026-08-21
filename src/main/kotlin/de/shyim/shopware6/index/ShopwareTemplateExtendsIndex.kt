package de.shyim.shopware6.index

import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileBasedIndexExtension
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.intellij.util.io.VoidDataExternalizer
import com.jetbrains.twig.TwigFileType
import de.shyim.shopware6.util.TwigUtil

/**
 * Reverse of [ShopwareTemplateIndex]: indexes every extending template by the view path its
 * sw_extends tag points to. This makes the templates extending a given view path a single index
 * lookup instead of a scan over every known template.
 */
class ShopwareTemplateExtendsIndex : FileBasedIndexExtension<String, Void>() {
    override fun getName(): ID<String, Void> {
        return key
    }

    override fun getIndexer(): DataIndexer<String, Void, FileContent> {
        return DataIndexer { inputData ->
            if (!inputData.file.path.contains("Resources/views/")) {
                return@DataIndexer mapOf()
            }

            val target = TwigUtil.findExtendsTargetReference(inputData.contentAsText)
                ?: return@DataIndexer mapOf()

            // overrides reference the template by bundle ("@Storefront/..."), but at runtime they
            // extend every template of that view path, so only the view path is indexed
            val viewPath = target.substringAfter("/", "")

            if (viewPath.isEmpty()) {
                return@DataIndexer mapOf()
            }

            mapOf(viewPath to null)
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): VoidDataExternalizer {
        return VoidDataExternalizer.INSTANCE
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
        val key = ID.create<String, Void>("de.shyim.shopware6.frontend.twig_template_extends")
    }
}
