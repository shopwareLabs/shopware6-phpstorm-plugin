package de.shyim.shopware6.test.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentOverrideIndex
import junit.framework.TestCase

class AdminComponentOverrideIndexTest: BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("button.js")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/AdminComponentOverrideIndex/"
    }

    fun testIsIndexed() {
        val keys = FileBasedIndex.getInstance().getAllKeys(AdminComponentOverrideIndex.key, project)

        TestCase.assertEquals(1, keys.size)

        val files = FileBasedIndex.getInstance().getValues(AdminComponentOverrideIndex.key, "sw-button", GlobalSearchScope.projectScope(project))
        TestCase.assertEquals("sw-button", files.first().name)
        TestCase.assertEquals("/src/button.js", files.first().file)
    }
}