package de.shyim.shopware6.test.navigation

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.twig.elements.TwigBlockTag
import de.shyim.shopware6.navigation.TwigBlockGoToDeclareHandler
import de.shyim.shopware6.navigation.TwigTemplateGoToDeclareHandler
import de.shyim.shopware6.util.TwigUtil

class TwigNavigationTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.copyDirectoryToProject("MyPluginOther", "MyPluginOther")
        myFixture.copyDirectoryToProject("custom", "custom")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/navigation/TwigNavigationTest/"
    }

    fun testTemplateNavigationResolvesAllTemplatesOfThePath() {
        myFixture.configureByText(
            "test.html.twig",
            "{% sw_extends '@Storefront/storefront/page/con<caret>tent/index.html.twig' %}"
        )

        val targets = TwigTemplateGoToDeclareHandler().getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset),
            myFixture.caretOffset,
            myFixture.editor
        )

        assertNotNull(targets)
        assertSame(2, targets!!.size)
        // the referenced bundle first, then the other plugin overriding the same path
        assertTrue(TwigUtil.isShopwareCoreTemplate(targets[0].containingFile.virtualFile.path))
        assertTrue(targets[1].containingFile.virtualFile.path.contains("MyPluginOther"))
    }

    fun testTemplateNavigationExcludesTheCurrentFile() {
        // the navigation source file overrides the referenced path itself and must not be offered
        val file = myFixture.addFileToProject(
            "MyPlugin/Resources/views/storefront/page/content/index.html.twig",
            "{% sw_extends '@Storefront/storefront/page/content/index.html.twig' %}\n"
        )
        myFixture.configureFromExistingVirtualFile(file.virtualFile)

        val element = myFixture.file.findElementAt(myFixture.file.text.indexOf("storefront/page"))

        val targets = TwigTemplateGoToDeclareHandler().getGotoDeclarationTargets(element, 0, myFixture.editor)

        assertNotNull(targets)
        assertSame(2, targets!!.size)
        assertFalse(targets.any { it.containingFile.virtualFile.path.contains("/MyPlugin/") })
    }

    fun testTemplateNavigationToCustomPluginsBundle() {
        myFixture.configureByText(
            "test.html.twig",
            "{% sw_include '@TcinnTheme/storefront/themeware/exam<caret>ple.html.twig' %}"
        )

        val targets = TwigTemplateGoToDeclareHandler().getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset),
            myFixture.caretOffset,
            myFixture.editor
        )

        assertNotNull(targets)
        assertSame(1, targets!!.size)
        assertTrue(targets[0].containingFile.virtualFile.path.contains("TcinnTheme"))
    }

    fun testBlockNavigationToParentBlock() {
        myFixture.configureByText(
            "test.html.twig",
            """
            {% sw_extends '@TcinnTheme/storefront/themeware/example.html.twig' %}

            {% block twt_exam<caret>ple_content %}
                <div>override</div>
            {% endblock %}
            """.trimIndent()
        )

        val targets = TwigBlockGoToDeclareHandler().getGotoDeclarationTargets(
            myFixture.file.findElementAt(myFixture.caretOffset),
            myFixture.caretOffset,
            myFixture.editor
        )

        assertNotNull(targets)
        assertSame(1, targets!!.size)
        assertTrue(targets[0] is TwigBlockTag)
        assertTrue(targets[0].containingFile.virtualFile.path.contains("TcinnTheme"))
    }

    fun testTemplateCompletion() {
        myFixture.configureByText("test.html.twig", "{% sw_extends '<caret>' %}")
        myFixture.completeBasic()

        val lookupStrings = myFixture.lookupElementStrings!!

        assertTrue(lookupStrings.contains("@Storefront/storefront/page/content/index.html.twig"))
        assertTrue(lookupStrings.contains("@TcinnTheme/storefront/themeware/example.html.twig"))
        assertTrue(lookupStrings.contains("@MyPluginOther/storefront/page/content/index.html.twig"))
    }
}
