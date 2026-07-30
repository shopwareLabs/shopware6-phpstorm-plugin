package de.shyim.shopware6.test.inspection

import com.intellij.psi.search.GlobalSearchScope
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import de.shyim.shopware6.index.TwigBlockHashIndex
import de.shyim.shopware6.inspection.twig.TwigBlockHashChanged

class TwigBlockHashChangedTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "src/test/testData/inspection/TwigBlockHashChangedTest/"
    }

    fun testChangedUpstreamBlockIsReported() {
        myFixture.copyDirectoryToProject("ShopwarePlatform", "ShopwarePlatform")
        myFixture.enableInspections(TwigBlockHashChanged())

        val currentHash = FileBasedIndex.getInstance().getValues(
            TwigBlockHashIndex.key,
            "base_content",
            GlobalSearchScope.allScope(project)
        ).first().hash

        val file = myFixture.addFileToProject(
            "MyPlugin/Resources/views/storefront/page/content/index.html.twig",
            """
            {# shopware-block: $currentHash@6.6.0.0 #}
            {% block base_content %}
                <div>override of unchanged upstream block</div>
            {% endblock %}

            {# shopware-block: outdatedhash@6.5.0.0 #}
            <warning descr="The upstream block has been changed, please update the block">{% block base_other %}
                <div>override of changed upstream block</div>
            {% endblock %}</warning>
            """.trimIndent()
        )

        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.checkHighlighting(true, false, true)
    }
}
