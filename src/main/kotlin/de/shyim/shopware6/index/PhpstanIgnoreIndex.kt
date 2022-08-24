package de.shyim.shopware6.index

import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import de.shyim.shopware6.index.dict.PhpstanIgnores
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer

class PhpstanIgnoreIndex : FileBasedIndexExtension<String, PhpstanIgnores>() {
    private val _externalizer = ObjectStreamDataExternalizer<PhpstanIgnores>()

    override fun getName(): ID<String, PhpstanIgnores> {
        return key
    }

    override fun getIndexer(): DataIndexer<String, PhpstanIgnores, FileContent> {
        return DataIndexer { inputData ->
            val lines = inputData.contentAsText.lines()

            val files = mutableMapOf<String, PhpstanIgnores>()

            var currentCount = 0

            lines.forEach { line ->
                val fixedLine = line.replace("\t", "").trim()

                if (fixedLine.isEmpty() || fixedLine.startsWith("parameters:") || fixedLine.startsWith("ignoreErrors:") || fixedLine == "-") {
                    return@forEach
                }

                if (fixedLine.startsWith("count:")) {
                    val count = fixedLine.substring(fixedLine.indexOf(":") + 1).trim()

                    currentCount += Integer.parseInt(count)
                    return@forEach
                }

                if (fixedLine.startsWith("path:")) {
                    val path = fixedLine.substring(fixedLine.indexOf(":") + 1).trim()

                    if (files.containsKey(path)) {
                        files[path]!!.errors += currentCount
                    } else {
                        files[path] = PhpstanIgnores(inputData.file.path, currentCount)
                    }

                    currentCount = 0
                    return@forEach
                }
            }

            return@DataIndexer files
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<PhpstanIgnores> {
        return _externalizer
    }

    override fun getVersion(): Int {
        return 2
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> file.name == "phpstan-baseline.neon" }
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    companion object {
        val key = ID.create<String, PhpstanIgnores>("de.shyim.shopware6.phpstan")
    }
}