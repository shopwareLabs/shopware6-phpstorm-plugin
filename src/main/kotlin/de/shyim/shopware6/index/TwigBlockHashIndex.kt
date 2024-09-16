package de.shyim.shopware6.index

import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.dict.TwigBlockHash
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer
import de.shyim.shopware6.util.StringUtil
import de.shyim.shopware6.util.TwigUtil

class TwigBlockHashIndex: FileBasedIndexExtension<String, TwigBlockHash>() {
    private val _externalizer = ObjectStreamDataExternalizer<TwigBlockHash>()

    override fun getName(): ID<String, TwigBlockHash> {
        return key
    }

    override fun getIndexer(): DataIndexer<String, TwigBlockHash, FileContent> {
        return DataIndexer { inputData ->
            val hashes = HashMap<String, TwigBlockHash>()

            if (!inputData.file.path.contains("src/Storefront/Resources/views/storefront") && !inputData.file.path.contains("vendor/shopware/storefront/Resources/views/storefront")) {
                return@DataIndexer mapOf()
            }

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: com.intellij.psi.PsiElement) {
                    if (element is TwigBlockTag && element.name !== null) {
                        hashes[element.name!!] = TwigBlockHash(element.name!!, TwigUtil.getRelativePath(inputData.file.path), inputData.file.path, StringUtil.sha512(element.parent.text), element.parent.text)
                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer hashes
        }
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<TwigBlockHash> {
        return _externalizer
    }

    override fun getVersion(): Int {
        return 1
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(TwigFileType.INSTANCE) {
        }
    }

    override fun dependsOnFileContent(): Boolean {
        return true;
    }

    companion object {
        val key = ID.create<String, TwigBlockHash>("de.shyim.shopware6.frontend.twig_hash")
    }
}