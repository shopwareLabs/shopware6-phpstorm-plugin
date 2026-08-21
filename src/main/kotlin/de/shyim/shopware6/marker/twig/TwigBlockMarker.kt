package de.shyim.shopware6.marker.twig

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.php.PhpIcons
import com.jetbrains.twig.TwigTokenTypes
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.util.TwigUtil

/**
 * Gutter icons on {% block name %} pointing to the block it overrides and to the blocks
 * overriding it, the same way PhpStorm marks overridden methods.
 */
class TwigBlockMarker : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        // the identifier leaf carries the marker, so the icon sits on the block's line
        if (element.elementType != TwigTokenTypes.IDENTIFIER) {
            return
        }

        val blockTag = element.parent as? TwigBlockTag ?: return
        val blockName = blockTag.name ?: return

        if (blockName != element.text) {
            return
        }

        val file = element.containingFile.originalFile

        // a template that does not extend cannot override a block. Without this check the
        // relative path fallback of getUpstreamBlocks would report the overrides of the very
        // same view path as upstream
        if (TwigUtil.isExtendingTemplate(file) && TwigUtil.getUpstreamBlocks(file, blockName).isNotEmpty()) {
            result.add(
                NavigationGutterIconBuilder.create(PhpIcons.OVERRIDES)
                    .setTargets(NotNullLazyValue.lazy { upstreamTargets(element, blockName) })
                    .setTooltipText("Overrides block")
                    .createLineMarkerInfo(element)
            )
        }

        // the paths come from the indexes, the PSI of the extending templates is only loaded
        // once the marker is clicked
        if (TwigUtil.getDownstreamBlockPaths(file, blockName).isNotEmpty()) {
            result.add(
                NavigationGutterIconBuilder.create(PhpIcons.IMPLEMENTED)
                    .setTargets(NotNullLazyValue.lazy { TwigUtil.getDownstreamBlocks(file, blockName) })
                    .setTooltipText("Overridden in extending templates")
                    .createLineMarkerInfo(element)
            )
        }
    }

    private fun upstreamTargets(element: PsiElement, blockName: String): Collection<PsiElement> {
        val file = element.containingFile.originalFile

        return TwigUtil.getUpstreamBlocks(file, blockName)
            .flatMap { TwigUtil.findBlockTagsInFile(element.project, it.absolutePath, blockName) }
    }
}
