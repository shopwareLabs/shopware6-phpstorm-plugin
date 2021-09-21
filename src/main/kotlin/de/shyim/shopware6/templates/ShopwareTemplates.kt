package de.shyim.shopware6.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import de.shyim.shopware6.action.generator.ui.NewChangelogConfig
import de.shyim.shopware6.action.generator.ui.NewComponentConfig
import icons.ShopwareToolBoxIcons

class ShopwareTemplates: FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("Shopware", ShopwareToolBoxIcons.SHOPWARE)

        FileTemplateGroupDescriptor("Contribution", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE))
        }

        FileTemplateGroupDescriptor("Administration", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT_SCSS))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT_TWIG))
        }

        FileTemplateGroupDescriptor("Plugin", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PLUGIN_CONFIG_TEMPLATE))
        }

        return group
    }

    companion object {
        const val SHOPWARE_ADMIN_VUE_COMPONENT = "Shopware Vue Component.js"
        const val SHOPWARE_ADMIN_VUE_COMPONENT_SCSS = "Shopware Vue Component SCSS.scss"
        const val SHOPWARE_ADMIN_VUE_COMPONENT_TWIG = "Shopware Vue Component Twig.html.twig"
        const val SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE = "Shopware CHANGELOG.md"
        const val SHOPWARE_PLUGIN_CONFIG_TEMPLATE = "Shopware Plugin config.xml"

        protected fun Project.applyTemplate(
            templateName: String,
            properties: Map<String, String>? = null
        ): String {
            val manager = FileTemplateManager.getInstance(this)
            val template = manager.getJ2eeTemplate(templateName)

            val allProperties = manager.defaultProperties
            properties?.let { prop -> allProperties.putAll(prop) }

            return template.getText(allProperties)
        }

        fun applyChangelogTemplate(project: Project, config: NewChangelogConfig): String {
            val props = mapOf(
                "TITLE" to config.title,
                "TICKET" to config.ticket,
                "FLAG" to config.flag,
            )

            return project.applyTemplate(SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE, props)
        }

        fun applyShopwareAdminVueComponent(project: Project, name: String, config: NewComponentConfig): String {
            val props = mapOf(
                "NAME" to config.name,
                "GENERATE_SCSS" to config.generateCss.toString(),
                "GENERATE_TWIG" to config.generateTwig.toString(),
            )

            return project.applyTemplate(name, props)
        }

        fun applyShopwarePluginConfig(project: Project): String {
            return project.applyTemplate(SHOPWARE_PLUGIN_CONFIG_TEMPLATE)
        }
    }

    private fun template(fileName: String, displayName: String? = null) = CustomDescriptor(fileName, displayName)

    private class CustomDescriptor(fileName: String, val visibleName: String?) : FileTemplateDescriptor(fileName) {
        override fun getDisplayName(): String = visibleName ?: fileName
    }
}