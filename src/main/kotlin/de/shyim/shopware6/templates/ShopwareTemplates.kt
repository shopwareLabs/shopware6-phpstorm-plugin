package de.shyim.shopware6.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import icons.ShopwareToolBoxIcons

class ShopwareTemplates : FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("Shopware", ShopwareToolBoxIcons.SHOPWARE)

        FileTemplateGroupDescriptor("Contribution", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE))
        }

        FileTemplateGroupDescriptor("PHP", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PHP_SCHEDULED_TASK))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PHP_SCHEDULED_TASK_HANDLER))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PHP_EVENT_LISTENER))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PHP_MIGRATION))
        }

        FileTemplateGroupDescriptor("Administration", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_MODULE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT_OVERRIDE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT_EXTEND))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT_SCSS))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT_TWIG))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_SCSS))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_STOREFRONT_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_COMPONENT_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_COMPONENT_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_COMPONENT_SCSS))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_CONFIG_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_CONFIG_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_CONFIG_SCSS))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_PREVIEW_INDEX))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_PREVIEW_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_PREVIEW_SCSS))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_CMS_ELEMENT_STOREFRONT))
        }

        FileTemplateGroupDescriptor("Plugin", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PLUGIN_CONFIG_TEMPLATE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PLUGIN_BOOTSTRAP))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PLUGIN_CHANGELOG))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_PLUGIN_COMPOSER_JSON))
        }

        FileTemplateGroupDescriptor("App", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_APP_MANIFEST))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_APP_HOOK))
        }

        return group
    }

    companion object {
        const val SHOPWARE_ADMIN_VUE_MODULE = "Shopware Vue Module.js"
        const val SHOPWARE_ADMIN_VUE_MODULE_SNIPPET_JSON = "Shopware Vue Module Snippet.json"
        const val SHOPWARE_ADMIN_VUE_COMPONENT = "Shopware Vue Component.js"
        const val SHOPWARE_ADMIN_VUE_COMPONENT_EXTEND = "Shopware Vue Component Extend.js"
        const val SHOPWARE_ADMIN_VUE_COMPONENT_OVERRIDE = "Shopware Vue Component Override.js"
        const val SHOPWARE_ADMIN_VUE_COMPONENT_SCSS = "Shopware Vue Component SCSS.scss"
        const val SHOPWARE_ADMIN_VUE_COMPONENT_TWIG = "Shopware Vue Component Twig.html.twig"
        const val SHOPWARE_ADMIN_CMS_BLOCK_INDEX = "Shopware Block Index.js"
        const val SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_INDEX = "Shopware Block Component Index.js"
        const val SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_TEMPLATE = "Shopware Block Component Template.twig"
        const val SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_INDEX = "Shopware Block Preview Index.js"
        const val SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_TEMPLATE = "Shopware Block Preview Template.twig"
        const val SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_SCSS = "Shopware Block Preview SCSS.scss"
        const val SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_STOREFRONT_TEMPLATE = "Shopware Block Storefront.twig"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_INDEX = "Shopware Element Index.js"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_COMPONENT_INDEX = "Shopware Element Component Index.js"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_COMPONENT_TEMPLATE = "Shopware Element Component Template.twig"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_COMPONENT_SCSS = "Shopware Element Component SCSS.scss"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_CONFIG_INDEX = "Shopware Element Config Index.js"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_CONFIG_TEMPLATE = "Shopware Element Config Template.twig"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_CONFIG_SCSS = "Shopware Element Config SCSS.scss"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_PREVIEW_INDEX = "Shopware Element Preview Index.js"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_PREVIEW_TEMPLATE = "Shopware Element Preview Template.twig"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_PREVIEW_SCSS = "Shopware Element Preview SCSS.scss"
        const val SHOPWARE_ADMIN_CMS_ELEMENT_STOREFRONT = "Shopware Element Storefront.twig"
        const val SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE = "Shopware CHANGELOG.md"
        const val SHOPWARE_PLUGIN_CONFIG_TEMPLATE = "Shopware Plugin config.xml"
        const val SHOPWARE_PHP_SCHEDULED_TASK = "Shopware PHP Scheduled Task.php"
        const val SHOPWARE_PHP_SCHEDULED_TASK_HANDLER = "Shopware PHP Scheduled TaskHandler.php"
        const val SHOPWARE_PHP_EVENT_LISTENER = "Shopware PHP Event Listener.php"
        const val SHOPWARE_PHP_MIGRATION = "Shopware PHP Migration.php"
        const val SHOPWARE_PLUGIN_BOOTSTRAP = "Shopware Plugin Bootstrap.php"
        const val SHOPWARE_PLUGIN_CHANGELOG = "Shopware Plugin Changelog.md"
        const val SHOPWARE_PLUGIN_COMPOSER_JSON = "Shopware Plugin composer.json"
        const val SHOPWARE_PLUGIN_SERVICES_PHP = "Shopware Plugin services.php"
        const val SHOPWARE_PLUGIN_ROUTES_PHP = "Shopware Plugin routes.php"
        const val SHOPWARE_APP_HOOK = "Shopware App script.twig"
        const val SHOPWARE_APP_MANIFEST = "Shopware App manifest.xml"
        const val SHOPWARE_APP_CUSTOM_ENTITIES = "Shopware App custom entities.xml"
        const val SHOPWARE_APP_CMS = "Shopware App CMS.xml"

        private fun Project.applyTemplate(
            templateName: String,
            properties: Map<String, String>? = null
        ): String {
            val manager = FileTemplateManager.getInstance(this)
            val template = manager.getJ2eeTemplate(templateName)

            val allProperties = manager.defaultProperties
            properties?.let { prop -> allProperties.putAll(prop) }

            return template.getText(allProperties)
        }

        fun renderTemplate(project: Project, templateName: String, properties: Map<String, String>?): String {
            return project.applyTemplate(templateName, properties)
        }
    }
}