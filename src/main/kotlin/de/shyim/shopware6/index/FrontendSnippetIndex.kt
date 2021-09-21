package de.shyim.shopware6.index

import com.intellij.json.JsonFileType
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.php.lang.psi.stubs.indexes.StringSetDataExternalizer
import de.shyim.shopware6.util.SnippetUtil

open class FrontendSnippetIndex : FileBasedIndexExtension<String, Set<String>>() {
    override fun getName(): ID<String, Set<String>> {
        return key
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, Set<String>, FileContent> {
        return DataIndexer { inputData ->
            if (!inputData.getFile().getName().equals("storefront.en-GB.json")) {
                return@DataIndexer mapOf()
            }

            return@DataIndexer SnippetUtil.flatten(inputData.contentAsText.toString())
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<Set<String>> {
        return StringSetDataExternalizer.INSTANCE
    }

    companion object {
        val key = ID.create<String, Set<String>>("de.shyim.shopware6.frontend.snippet")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {
        }
    }
}