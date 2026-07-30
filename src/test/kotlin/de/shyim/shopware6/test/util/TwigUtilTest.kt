package de.shyim.shopware6.test.util

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.util.TwigUtil

class TwigUtilTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testData/util/TwigUtilTest/"
    }

    fun testVersioningCommentUsesExtensionComposerVersion() {
        myFixture.copyDirectoryToProject("custom", "custom")

        val comment =
            TwigUtil.getVersioningComment(project, "twt_example_content", "storefront/themeware/example.html.twig")

        assertNotNull(comment)
        assertTrue(comment!!.endsWith("@2.5.0 #}\n"))
    }

    fun testVersioningCommentIgnoresCommentedOverridesOfOtherPlugins() {
        myFixture.copyDirectoryToProject("custom", "custom")

        // AaaCommentedPlugin overrides the same block with a versioning comment and would win the
        // alphabetical ordering, but overrides must never be used as the upstream hash
        val themeHash = FileBasedIndex.getInstance().getValues(
            TwigBlockHashIndex.key,
            "twt_example_content",
            GlobalSearchScope.allScope(project)
        ).first { it.absolutePath.contains("TcinnTheme") }.hash

        val comment =
            TwigUtil.getVersioningComment(project, "twt_example_content", "storefront/themeware/example.html.twig")

        assertNotNull(comment)
        assertTrue(comment!!.contains(themeHash))
    }

    fun testVersioningCommentFollowsExtendsChain() {
        myFixture.copyDirectoryToProject("custom", "custom")

        // the source file extends the theme template at a different relative path, so the
        // upstream block can only be resolved through the sw_extends chain
        val file = myFixture.addFileToProject(
            "MyPlugin/Resources/views/storefront/custom-page/index.html.twig",
            """
            {% sw_extends '@TcinnTheme/storefront/themeware/example.html.twig' %}

            {% block twt_example_content %}
                <div>reuse</div>
            {% endblock %}
            """.trimIndent()
        )

        val comment = TwigUtil.getVersioningComment(
            project,
            "twt_example_content",
            "storefront/custom-page/index.html.twig",
            file
        )

        assertNotNull(comment)
        assertTrue(comment!!.endsWith("@2.5.0 #}\n"))
    }

    fun testVersioningCommentWithoutKnownVersionHasNoVersionSuffix() {
        myFixture.copyDirectoryToProject("custom", "custom")

        val comment = TwigUtil.getVersioningComment(project, "nv_content", "storefront/noversion/index.html.twig")

        assertNotNull(comment)
        assertFalse(comment!!.contains("@"))
    }
}
