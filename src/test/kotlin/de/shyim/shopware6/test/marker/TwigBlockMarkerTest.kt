package de.shyim.shopware6.test.marker

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.psi.PsiElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class TwigBlockMarkerTest : BasePlatformTestCase() {
    private val corePath =
        "ShopwarePlatform/src/Storefront/Resources/views/storefront/page/content/index.html.twig"
    private val pluginPath = "MyPlugin/Resources/views/storefront/page/content/index.html.twig"
    private val themePath =
        "custom/plugins/TcinnTheme/src/Resources/views/storefront/page/content/index.html.twig"

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.copyDirectoryToProject("custom", "custom")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/marker/TwigBlockMarkerTest/"
    }

    fun testUpstreamBlockIsOnlyMarkedAsOverridden() {
        // the core template extends nothing, so it can only be overridden
        assertEquals(listOf("Overridden in extending templates"), tooltipsOf(corePath))
    }

    fun testBlockInTheMiddleOfTheChainIsMarkedInBothDirections() {
        assertEquals(
            listOf("Overrides block", "Overridden in extending templates"),
            tooltipsOf(pluginPath)
        )
    }

    fun testLastOverrideOfTheChainIsOnlyMarkedAsOverriding() {
        // the theme is the last template of the chain, the plugin it extends is upstream and
        // must not be reported as an override of it
        assertEquals(listOf("Overrides block"), tooltipsOf(themePath))
    }

    fun testBlockWithoutRelatedBlocksHasNoMarker() {
        openFile(corePath)

        val untouchedLine = lineOfText("base_untouched")

        assertTrue(markers().none { line(it) == untouchedLine })
    }

    fun testOverridesMarkerNavigatesUpTheChain() {
        openFile(themePath)

        val paths = targetsOf("Overrides block").map { it.containingFile.virtualFile.path }

        // the whole chain is offered, nearest parent first
        assertEquals(2, paths.size)
        assertTrue(paths[0].contains("/MyPlugin/"))
        assertTrue(paths[1].contains("ShopwarePlatform"))
    }

    fun testOverriddenMarkerNavigatesToEveryOverride() {
        openFile(corePath)

        val paths = targetsOf("Overridden in extending templates")
            .map { it.containingFile.virtualFile.path }

        assertEquals(2, paths.size)
        assertTrue(paths.any { it.contains("/MyPlugin/") })
        assertTrue(paths.any { it.contains("TcinnTheme") })
    }

    private fun openFile(path: String) {
        myFixture.configureFromExistingVirtualFile(myFixture.findFileInTempDir(path))
    }

    private fun tooltipsOf(path: String): List<String> {
        openFile(path)

        return markers().mapNotNull { it.lineMarkerInfo.lineMarkerTooltip }
    }

    private fun targetsOf(tooltip: String): Collection<PsiElement> {
        val marker = markers().first { it.lineMarkerInfo.lineMarkerTooltip == tooltip }

        return (marker.lineMarkerInfo as RelatedItemLineMarkerInfo<*>).createGotoRelatedItems()
            .mapNotNull { it.element }
    }

    private fun markers(): List<LineMarkerInfo.LineMarkerGutterIconRenderer<*>> {
        return myFixture.findAllGutters()
            .filterIsInstance<LineMarkerInfo.LineMarkerGutterIconRenderer<*>>()
    }

    private fun line(marker: LineMarkerInfo.LineMarkerGutterIconRenderer<*>): Int {
        return myFixture.editor.document.getLineNumber(marker.lineMarkerInfo.startOffset)
    }

    private fun lineOfText(text: String): Int {
        return myFixture.editor.document.text.lineSequence().indexOfFirst { it.contains(text) }
    }
}
