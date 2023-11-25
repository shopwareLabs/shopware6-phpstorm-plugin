package de.shyim.shopware6.index

import com.intellij.psi.PsiRecursiveElementWalkingVisitor
import com.intellij.util.indexing.*
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import com.jetbrains.twig.TwigFileType
import com.jetbrains.twig.elements.TwigBlockStatement
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.dict.TwigDeprecation
import de.shyim.shopware6.index.externalizer.ObjectStreamDataExternalizer

class TwigBlockDeprecationIndex : FileBasedIndexExtension<String, TwigDeprecation>() {
    private val _externalizer = ObjectStreamDataExternalizer<TwigDeprecation>()

    override fun getIndexer(): DataIndexer<String, TwigDeprecation, FileContent> {
        return DataIndexer { inputData ->
            val deprecations = HashMap<String, TwigDeprecation>()

            if (!inputData.file.path.contains("Resources/views")) {
                return@DataIndexer mapOf()
            }

            inputData.psiFile.acceptChildren(object : PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: com.intellij.psi.PsiElement) {
                    if (element is TwigBlockStatement && element.firstChild is TwigBlockTag) {
                        val blockName = (element.firstChild as TwigBlockTag).name!!

                        if (element.prevSibling !== null && element.prevSibling.prevSibling !== null && element.prevSibling.prevSibling.text.contains(
                                "@deprecated"
                            )
                        ) {
                            var message = ""

                            if (element.prevSibling.prevSibling.text.contains(" - ")) {
                                message = element.prevSibling.prevSibling.text.substringAfter(" - ")
                                message = message.substringBefore("#}").trim()
                            }

                            deprecations[blockName] =
                                TwigDeprecation(blockName, getRelativePath(inputData.file.path), message)
                        }

                    }

                    super.visitElement(element)
                }
            })

            return@DataIndexer deprecations
        }
    }

    override fun getName(): ID<String, TwigDeprecation> {
        return key
    }

    override fun getVersion(): Int {
        return 2
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): ObjectStreamDataExternalizer<TwigDeprecation> {
        return _externalizer
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return object : DefaultFileTypeSpecificInputFilter(TwigFileType.INSTANCE) {
        }
    }

    companion object {
        public fun getRelativePath(path: String): String {
            return path.substringAfter("Resources/views/")
        }

        val key = ID.create<String, TwigDeprecation>("de.shyim.shopware6.frontend.twig_blocks")
    }
}
