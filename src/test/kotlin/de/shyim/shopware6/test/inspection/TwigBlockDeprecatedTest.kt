package de.shyim.shopware6.test.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.shyim.shopware6.inspection.twig.TwigBlockDeprecated

class TwigBlockDeprecatedTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testData/inspection/TwigBlockDeprecatedTest/"
    }

    fun testDeprecatedBlocksAreReported() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.enableInspections(TwigBlockDeprecated())

        myFixture.configureFromTempProjectFile("MyPlugin/Resources/views/storefront/page/index.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }
}
