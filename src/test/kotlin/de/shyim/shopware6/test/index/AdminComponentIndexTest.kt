package de.shyim.shopware6.test.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminComponentIndex
import junit.framework.TestCase

class AdminComponentIndexTest: BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("button.js")
        myFixture.copyFileToProject("extended-button.js")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/AdminComponentIndex/"
    }

    fun testComponentsAreRegistered() {
        val keys = FileBasedIndex.getInstance().getAllKeys(AdminComponentIndex.key, project)
        TestCase.assertSame(2, keys.size)

        val button = FileBasedIndex.getInstance()
            .getValues(AdminComponentIndex.key, "sw-button", GlobalSearchScope.allScope(project)).first()

        TestCase.assertEquals("sw-button", button.name)
        TestCase.assertEquals(null, button.extends)
        TestCase.assertEquals("/src/button.js", button.file)
        TestCase.assertEquals("/src/sw-button.html.twig", button.templatePath)
        TestCase.assertEquals(3, button.props.size)

        val sorted = button.props.sortedDescending()

        TestCase.assertEquals("variant", sorted.elementAt(0))
        TestCase.assertEquals("size", sorted.elementAt(1))
        TestCase.assertEquals("disabled", sorted.elementAt(2))

        val extendedButton = FileBasedIndex.getInstance()
            .getValues(AdminComponentIndex.key, "sw-extended-button", GlobalSearchScope.allScope(project)).first()

        TestCase.assertEquals("sw-extended-button", extendedButton.name)
        TestCase.assertEquals("sw-button", extendedButton.extends)
        TestCase.assertEquals(null, extendedButton.templatePath)
        TestCase.assertEquals(0, extendedButton.props.size)
    }
}