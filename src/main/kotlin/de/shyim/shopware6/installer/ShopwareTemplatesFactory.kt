package de.shyim.shopware6.installer

import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.platform.ProjectTemplate
import com.intellij.platform.ProjectTemplatesFactory

class ShopwareTemplatesFactory : ProjectTemplatesFactory() {
    override fun getGroups(): Array<String> {
        return arrayOf("PHP", "Shopware")
    }

    override fun createTemplates(group: String?, context: WizardContext): Array<ProjectTemplate> {
        return arrayOf(ShopwareProjectGenerator())
    }
}