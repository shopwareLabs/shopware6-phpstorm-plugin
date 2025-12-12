package de.shyim.shopware6.test.index

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.AdminModuleIndex
import de.shyim.shopware6.util.AdminModuleUtil
import junit.framework.TestCase

class AdminModuleIndexTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyFileToProject("module/sw-product/index.js")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/AdminModuleIndex/"
    }

    fun testModuleAreRegistered() {
        val keys = FileBasedIndex.getInstance().getAllKeys(AdminModuleIndex.key, project)
        assertSame(1, keys.size)

        val values = FileBasedIndex.getInstance()
            .getValues(AdminModuleIndex.key, keys.first(), GlobalSearchScope.allScope(project))
        val module = values.first()

        TestCase.assertEquals("sw-product", module.name)
        assertSame(4, module.routes.size)

        var route = module.routes["sw.product.index"]!!
        TestCase.assertEquals("sw.product.index", route.name)
        TestCase.assertEquals("sw-product-list", route.component)

        route = module.routes["sw.product.detail"]!!
        TestCase.assertEquals("sw.product.detail", route.name)
        TestCase.assertEquals("sw-product-detail", route.component)

        route = module.routes["sw.product.detail.base"]!!
        TestCase.assertEquals("sw.product.detail.base", route.name)
        TestCase.assertEquals("sw-product-detail-base", route.component)

        route = module.routes["sw.product.detail.specifications"]!!
        TestCase.assertEquals("sw.product.detail.specifications", route.name)
        TestCase.assertEquals("sw-product-detail-specifications", route.component)
    }

    fun testLookupElements() {
        val lookupStrings = mutableListOf<String>()

        AdminModuleUtil.getAllRouteLookupItems(project).forEach {
            lookupStrings.add(it.lookupString)
        }

        assertTrue(lookupStrings.contains("sw.product.index"))
        assertTrue(lookupStrings.contains("sw.product.detail"))
        assertTrue(lookupStrings.contains("sw.product.detail.base"))
        assertTrue(lookupStrings.contains("sw.product.detail.specifications"))
    }
}