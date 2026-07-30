package de.shyim.shopware6.test.util

import com.intellij.testFramework.fixtures.BasePlatformTestCase
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

    fun testVersioningCommentWithoutKnownVersionHasNoVersionSuffix() {
        myFixture.copyDirectoryToProject("custom", "custom")

        val comment = TwigUtil.getVersioningComment(project, "nv_content", "storefront/noversion/index.html.twig")

        assertNotNull(comment)
        assertFalse(comment!!.contains("@"))
    }
}
