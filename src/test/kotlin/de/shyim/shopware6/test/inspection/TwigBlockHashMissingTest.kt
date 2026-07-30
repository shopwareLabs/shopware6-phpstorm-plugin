package de.shyim.shopware6.test.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.shyim.shopware6.inspection.twig.TwigBlockHashMissing

class TwigBlockHashMissingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testData/inspection/TwigBlockHashMissingTest/"
    }

    fun testBlocksWithoutVersioningCommentAreReported() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.enableInspections(TwigBlockHashMissing())

        myFixture.configureFromTempProjectFile("MyPlugin/Resources/views/storefront/page/content/index.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testShopwareCoreTemplatesAreNotReported() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.enableInspections(TwigBlockHashMissing())

        myFixture.configureFromTempProjectFile("ShopwarePlatform/src/Storefront/Resources/views/storefront/page/content/index.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }
}
