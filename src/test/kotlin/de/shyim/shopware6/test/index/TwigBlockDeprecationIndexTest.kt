package de.shyim.shopware6.test.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.TwigBlockDeprecationIndex

class TwigBlockDeprecationIndexTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("MyApp", "MyApp")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/TwigBlockDeprecationIndexTest/"
    }

    fun testDeprecationFound() {
        val keys = FileBasedIndex.getInstance().getAllKeys(TwigBlockDeprecationIndex.key, project)
        assertSame(1, keys.size)
        assertEquals("component_line_item_type_product_order_number", keys.first())

        var deprecations = FileBasedIndex.getInstance().getValues(
            TwigBlockDeprecationIndex.key,
            "component_line_item_type_product_order_number",
            GlobalSearchScope.allScope(project)
        )
        assertSame(1, deprecations.size)

        var deprecation = deprecations.first()

        assertEquals("component_line_item_type_product_order_number", deprecation.name)
        assertEquals("start-page/index.html.twig", deprecation.relPath)
        assertEquals(
            "Block will be removed. Use `component_line_item_type_product_number` instead.",
            deprecation.message
        )
    }
}