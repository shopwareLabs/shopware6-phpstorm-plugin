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

    fun testCommentedOverrideOfAnotherPluginDoesNotMaskTheRemoval() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        // another plugin still overrides the removed block, but its versioning comment marks it as
        // an override, so it cannot prove that the block still exists upstream
        myFixture.copyDirectoryToProject("MyPluginB", "MyPluginB")
        myFixture.enableInspections(TwigBlockRemoved())

        myFixture.configureFromTempProjectFile("MyPlugin/Resources/views/storefront/page/content/index.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testCommentedUpstreamInExtendsChainStillCounts() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("custom", "custom")
        myFixture.enableInspections(TwigBlockRemoved())

        // the extended theme uses versioning comments itself, but as part of the sw_extends
        // chain it is still the upstream of this override
        val file = myFixture.addFileToProject(
            "MyPlugin/Resources/views/storefront/commented/index.html.twig",
            """
            {% sw_extends '@CommentedTheme/storefront/commented/index.html.twig' %}

            {# shopware-block: myhash@1.0.0 #}
            {% block commented_theme_block %}
                <div>override</div>
            {% endblock %}
            """.trimIndent()
        )

        myFixture.configureFromExistingVirtualFile(file.virtualFile)
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
