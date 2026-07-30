package de.shyim.shopware6.inspection.twig

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.jetbrains.twig.TwigFile
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.util.TwigUtil

class TwigBlockRemoved : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file

        if (file !is TwigFile) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        val filePath = file.originalFile.virtualFile?.path ?: return super.buildVisitor(holder, isOnTheFly)

        // a block can only be recognized as removed when the extended template is part of the
        // project. Otherwise (e.g. a standalone plugin repository without the Shopware sources)
        // every block would be reported as removed
        val chainPaths = TwigUtil.getExtendsChainPaths(file)
        if (chainPaths.isEmpty()) {
            return super.buildVisitor(holder, isOnTheFly)
        }

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is TwigBlockTag && element.name !== null && TwigUtil.getShopwareBlockComment(element) !== null) {
                    if (TwigUtil.getUpstreamBlocks(element.project, filePath, chainPaths, element.name!!)
                            .isNotEmpty()
                    ) {
                        return
                    }

                    // blocks with a versioning comment themselves are overrides of another plugin,
                    // they cannot prove that the block still exists upstream
                    val otherLocations = FileBasedIndex.getInstance().getValues(
                        TwigBlockHashIndex.key,
                        element.name!!,
                        GlobalSearchScope.allScope(element.project)
                    )
                        .filter { it.absolutePath != filePath && !it.hasVersioningComment }
                        .map { it.relativePath }
                        .distinct()

                    if (otherLocations.isEmpty()) {
                        holder.registerProblem(
                            element.parent,
                            "The upstream block has been removed, please check if your override is still needed",
                            ProblemHighlightType.WARNING
                        )
                    } else {
                        holder.registerProblem(
                            element.parent,
                            "The upstream block has been removed from this template, but still exists in: ${
                                otherLocations.joinToString(", ")
                            }",
                            ProblemHighlightType.WARNING
                        )
                    }
                }
            }
        }
    }
}
