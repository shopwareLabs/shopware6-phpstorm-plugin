package de.shyim.shopware6.test.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminSnippetIndex
import de.shyim.shopware6.util.AdminSnippetUtil
import junit.framework.TestCase

class AdminSnippetIndexTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("snippet/de-DE.json")
        myFixture.copyFileToProject("snippet/en-GB.json")
        myFixture.copyFileToProject("other/en-GB.json")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/AdminSnippetIndex/"
    }

    fun testSnippetsAreIndexed() {
        assertTrue(AdminSnippetUtil.hasSnippet("test.foo", project))

        val values = FileBasedIndex.getInstance()
            .getValues(AdminSnippetIndex.key, "/src/snippet/en-GB.json", GlobalSearchScope.allScope(project))
        val snippetFile = values.first()
        TestCase.assertEquals("/src/snippet/en-GB.json", snippetFile.file)
        TestCase.assertEquals("EN", snippetFile.snippets["test.foo"])
        TestCase.assertEquals("EN-2", snippetFile.snippets["test.foo2"])
    }

    fun testNotExistingKey() {
        assertFalse(AdminSnippetUtil.hasSnippet("test.foo-not-existing", project))
    }

    fun testLookupWorks() {
        val lookupItems = AdminSnippetUtil.getAllLookupItems(project)
        assertSame(2, lookupItems.size)

        val lookupStrings = listOf(lookupItems[0].lookupString, lookupItems[1].lookupString)

        assertTrue(lookupStrings.contains("test.foo"))
        assertTrue(lookupStrings.contains("test.foo2"))
    }
}