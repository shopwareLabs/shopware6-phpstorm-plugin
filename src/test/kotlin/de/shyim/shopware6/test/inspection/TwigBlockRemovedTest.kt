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
}
