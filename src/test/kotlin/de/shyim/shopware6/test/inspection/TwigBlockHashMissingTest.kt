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

    fun testBlocksFromExtendedTemplateAtDifferentPathAreReported() {
        myFixture.copyDirectoryToProject("custom", "custom")
        myFixture.enableInspections(TwigBlockHashMissing())

        // the template reuses another template at a different relative path, so the upstream
        // block can only be found through the sw_extends chain
        val file = myFixture.addFileToProject(
            "MyPlugin/Resources/views/storefront/custom-page/index.html.twig",
            """
            {% sw_extends '@TcinnTheme/storefront/themeware/example.html.twig' %}

            <warning descr="The block does not have a versioning comment">{% block twt_example_content %}
                <div>reuse of the theme template</div>
            {% endblock %}</warning>
            """.trimIndent()
        )

        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.checkHighlighting(true, false, true)
    }

    fun testBlocksOnlyKnownFromCommentedOverridesAreNotReported() {
        myFixture.enableInspections(TwigBlockHashMissing())

        // the only other occurrence of the block is itself an override tracking an upstream
        // that is not part of this project, so there is nothing to version against
        myFixture.addFileToProject(
            "OtherPlugin/Resources/views/storefront/orphan/index.html.twig",
            """
            {% sw_extends '@Storefront/storefront/orphan/index.html.twig' %}

            {# shopware-block: somehash@6.6.0.0 #}
            {% block orphan_block %}
                <div>tracked override</div>
            {% endblock %}
            """.trimIndent()
        )

        val file = myFixture.addFileToProject(
            "MyPlugin/Resources/views/storefront/orphan/index.html.twig",
            """
            {% sw_extends '@Storefront/storefront/orphan/index.html.twig' %}

            {% block orphan_block %}
                <div>my override</div>
            {% endblock %}
            """.trimIndent()
        )

        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.checkHighlighting(true, false, true)
    }

    fun testCustomPluginsThirdPartyBlocksAreReported() {
        myFixture.copyDirectoryToProject("custom", "custom")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.enableInspections(TwigBlockHashMissing())

        myFixture.configureFromTempProjectFile("MyPlugin/Resources/views/storefront/themeware/example.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testCustomPluginsSourceTemplatesAreNotReported() {
        myFixture.copyDirectoryToProject("custom", "custom")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.enableInspections(TwigBlockHashMissing())

        // the theme file itself does not extend anything, so it must not be treated as an override
        myFixture.configureFromTempProjectFile("custom/plugins/TcinnTheme/src/Resources/views/storefront/themeware/example.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testThirdPartyExtensionBlocksAreReported() {
        myFixture.copyDirectoryToProject("vendor", "vendor")
        myFixture.copyDirectoryToProject("MyPlugin", "MyPlugin")
        myFixture.enableInspections(TwigBlockHashMissing())

        myFixture.configureFromTempProjectFile("MyPlugin/Resources/views/storefront/component/example.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testThirdPartyExtensionTemplatesThemselvesAreNotReported() {
        myFixture.copyDirectoryToProject("vendor", "vendor")
        myFixture.enableInspections(TwigBlockHashMissing())

        myFixture.configureFromTempProjectFile("vendor/acme/example-plugin/Resources/views/storefront/component/example.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }

    fun testShopwareCoreTemplatesAreNotReported() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.enableInspections(TwigBlockHashMissing())

        myFixture.configureFromTempProjectFile("ShopwarePlatform/src/Storefront/Resources/views/storefront/page/content/index.html.twig")
        myFixture.checkHighlighting(true, false, true)
    }
}
