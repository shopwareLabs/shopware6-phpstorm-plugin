package de.shyim.shopware6.index

import com.intellij.json.JsonFileType
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.SnippetFile
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import de.shyim.shopware6.util.SnippetUtil


open class FrontendSnippetIndex : FileBasedIndexExtension<String, SnippetFile>() {
    private val _externalizer = ObjectStreamDataExternalizer<SnippetFile>()

    override fun getName(): ID<String, SnippetFile> {
        return key
    }

    override fun getVersion(): Int {
        return 4
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getIndexer(): DataIndexer<String, SnippetFile, FileContent> {
        return DataIndexer { inputData ->
            if (!inputData.file.path.contains("/Resources/snippet/") || inputData.file.path.contains("/_fixtures/")) {
                return@DataIndexer mapOf()
            }

            val snippets = HashMap<String, SnippetFile>()
            snippets[inputData.file.path] = SnippetUtil.flatten(inputData.file.path, inputData.contentAsText.toString())

            return@DataIndexer snippets
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<SnippetFile> {
        return _externalizer
    }

    companion object {
        val key = ID.create<String, SnippetFile>("de.shyim.shopware6.frontend.snippet")
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(JsonFileType.INSTANCE) {
        }
    }
}