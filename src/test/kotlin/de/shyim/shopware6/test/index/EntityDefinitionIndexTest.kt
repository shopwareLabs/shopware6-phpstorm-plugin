package de.shyim.shopware6.test.index

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import de.shyim.shopware6.util.EntityDefinitionUtil
import junit.framework.TestCase

class EntityDefinitionIndexTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
    }

    override fun getTestDataPath(): String {
        return "src/test/testData/index/EntityDefinitionIndexTest/"
    }

    fun testMailArchive() {
        myFixture.copyFileToProject("MailArchiveDefinition.php")

        val definitions = EntityDefinitionUtil.getAllDefinitions(project)
        assertSame(1, definitions.size)
        val definition = definitions.first()
        TestCase.assertEquals("frosh_mail_archive", definition.name)
        TestCase.assertEquals("\\Frosh\\MailArchive\\Content\\MailArchive\\MailArchiveDefinition", definition.fqn)
        assertSame(11, definition.fields.size)

        TestCase.assertEquals("id", definition.fields[0].name)
        assertFalse(definition.fields[0].association)
        TestCase.assertEquals("", definition.fields[0].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\IdField",
            definition.fields[0].type
        )

        TestCase.assertEquals("sender", definition.fields[1].name)
        assertFalse(definition.fields[1].association)
        TestCase.assertEquals("", definition.fields[1].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\JsonField",
            definition.fields[1].type
        )

        TestCase.assertEquals("receiver", definition.fields[2].name)
        assertFalse(definition.fields[2].association)
        TestCase.assertEquals("", definition.fields[2].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\JsonField",
            definition.fields[2].type
        )

        TestCase.assertEquals("subject", definition.fields[3].name)
        assertFalse(definition.fields[3].association)
        TestCase.assertEquals("", definition.fields[3].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\StringField",
            definition.fields[3].type
        )

        TestCase.assertEquals("plainText", definition.fields[4].name)
        assertFalse(definition.fields[4].association)
        TestCase.assertEquals("", definition.fields[4].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\LongTextField",
            definition.fields[4].type
        )

        TestCase.assertEquals("htmlText", definition.fields[5].name)
        assertFalse(definition.fields[5].association)
        TestCase.assertEquals("", definition.fields[5].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\LongTextField",
            definition.fields[5].type
        )

        TestCase.assertEquals("eml", definition.fields[6].name)
        assertFalse(definition.fields[6].association)
        TestCase.assertEquals("", definition.fields[6].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\LongTextField",
            definition.fields[6].type
        )

        TestCase.assertEquals("salesChannelId", definition.fields[7].name)
        assertFalse(definition.fields[7].association)
        TestCase.assertEquals("", definition.fields[7].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\FkField",
            definition.fields[7].type
        )

        TestCase.assertEquals("salesChannel", definition.fields[8].name)
        assertTrue(definition.fields[8].association)
        TestCase.assertEquals(
            "\\Shopware\\Core\\System\\SalesChannel\\SalesChannelDefinition",
            definition.fields[8].associationTarget
        )
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ManyToOneAssociationField",
            definition.fields[8].type
        )

        TestCase.assertEquals("customerId", definition.fields[9].name)
        assertFalse(definition.fields[9].association)
        TestCase.assertEquals("", definition.fields[9].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\FkField",
            definition.fields[9].type
        )

        TestCase.assertEquals("customer", definition.fields[10].name)
        assertTrue(definition.fields[10].association)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Checkout\\Customer\\CustomerDefinition",
            definition.fields[10].associationTarget
        )
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\ManyToOneAssociationField",
            definition.fields[10].type
        )
    }

    fun testShippingMethod() {
        myFixture.copyFileToProject("AppShippingMethodDefinition.php")

        val definitions = EntityDefinitionUtil.getAllDefinitions(project)
        assertSame(1, definitions.size)
        val definition = definitions.first()
        TestCase.assertEquals("app_shipping_method", definition.name)
        TestCase.assertEquals("\\Shopware\\Core\\Framework\\App\\Aggregate\\AppShippingMethod\\AppShippingMethodDefinition", definition.fqn)
        assertSame(9, definition.fields.size)

        TestCase.assertEquals("id", definition.fields[0].name)
        assertFalse(definition.fields[0].association)
        TestCase.assertEquals("", definition.fields[0].associationTarget)
        TestCase.assertEquals(
            "\\Shopware\\Core\\Framework\\DataAbstractionLayer\\Field\\IdField",
            definition.fields[0].type
        )

        TestCase.assertEquals("appName", definition.fields[1].name)
        TestCase.assertEquals("identifier", definition.fields[2].name)
        TestCase.assertEquals("appId", definition.fields[3].name)
        TestCase.assertEquals("app", definition.fields[4].name)
        TestCase.assertEquals("shippingMethodId", definition.fields[5].name)
        TestCase.assertEquals("shippingMethod", definition.fields[6].name)
        TestCase.assertEquals("originalMediaId", definition.fields[7].name)
        TestCase.assertEquals("originalMedia", definition.fields[8].name)
    }
}