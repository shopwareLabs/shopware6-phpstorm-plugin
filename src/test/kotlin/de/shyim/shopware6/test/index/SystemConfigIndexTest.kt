package de.shyim.shopware6.test.index

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.shyim.shopware6.util.SystemConfigUtil

class SystemConfigIndexTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("src", "src")
        myFixture.copyFileToProject("composer.json")
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/SystemConfigIndexTest/"
    }

    fun testConfigElementsAreThere() {
        val configs = SystemConfigUtil.getAllConfigs(project)

        assertEquals(21, configs.size)
        assertEquals("SwagFooBar.config", configs[0].namespace)
        assertEquals("senderAddressFirstName", configs[0].name)
        assertEquals("customsInformationComment", configs[20].name)
    }
}