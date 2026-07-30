package de.shyim.shopware6.test.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.shyim.shopware6.inspection.twig.TwigBlockRemoved

class TwigBlockRemovedTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testData/inspection/TwigBlockRemovedTest/"
    }

    fun testRemovedBlocksAreReported() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.enableInspections(TwigBlockRemoved())

        myFixture.configureFromTempProjectFile("MyPlugin/Resources/views/storefront/page/content/index.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testNothingIsReportedWithoutShopwareSources() {
        myFixture.enableInspections(TwigBlockRemoved())

        // a standalone plugin repository without the Shopware sources cannot know if a block was removed
        val file = myFixture.addFileToProject(
            "StandalonePlugin/Resources/views/storefront/page/index.html.twig",
            """
            {# shopware-block: somehash@6.6.0.0 #}
            {% block some_block %}
                <div>override</div>
            {% endblock %}
            """.trimIndent()
        )

        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.checkHighlighting(true, false, true)
    }
}
