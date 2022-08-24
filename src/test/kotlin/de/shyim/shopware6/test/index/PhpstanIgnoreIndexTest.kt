package de.shyim.shopware6.test.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.PhpstanIgnoreIndex

class PhpstanIgnoreIndexTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("phpstan-baseline.neon")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/PhpstanIgnoreIndexTest/"
    }

    fun testErrorsAreIndexed() {
        val keys = FileBasedIndex.getInstance().getAllKeys(PhpstanIgnoreIndex.key, project)

        assertEquals(2, keys.size)

        val controller = FileBasedIndex.getInstance()
            .getValues(
                PhpstanIgnoreIndex.key,
                "src/Administration/Controller/AdministrationController.php",
                GlobalSearchScope.allScope(project)
            ).first()

        val deprecation = FileBasedIndex.getInstance()
            .getValues(
                PhpstanIgnoreIndex.key,
                "src/Administration/Controller/DocumentServiceDeprecationController.php",
                GlobalSearchScope.allScope(project)
            ).first()

        assertEquals(3, controller.errors)
        assertEquals(1, deprecation.errors)
    }
}