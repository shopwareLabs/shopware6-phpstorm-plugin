package de.shyim.shopware6.templates

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.project.Project
import de.shyim.shopware6.action.generator.cms.NewCmsBlockConfig
import de.shyim.shopware6.action.generator.php.NewPluginConfig
import de.shyim.shopware6.action.generator.php.NewScheduledTaskConfig
import de.shyim.shopware6.action.generator.ui.NewChangelogConfig
import de.shyim.shopware6.action.generator.vue.NewComponentConfig
import de.shyim.shopware6.action.generator.vue.NewModuleConfig
import icons.ShopwareToolBoxIcons

class ShopwareTemplates: FileTemplateGroupDescriptorFactory {
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
        }

        FileTemplateGroupDescriptor("Administration", ShopwareToolBoxIcons.SHOPWARE).let { pluginGroup ->
            group.addTemplate(pluginGroup)
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_MODULE))
            pluginGroup.addTemplate(FileTemplateDescriptor(SHOPWARE_ADMIN_VUE_COMPONENT))
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

        return group
    }

    companion object {
        const val SHOPWARE_ADMIN_VUE_MODULE = "Shopware Vue Module.js"
        const val SHOPWARE_ADMIN_VUE_MODULE_SNIPPET_JSON = "Shopware Vue Module Snippet.json"
        const val SHOPWARE_ADMIN_VUE_COMPONENT = "Shopware Vue Component.js"
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
        const val SHOPWARE_PLUGIN_BOOTSTRAP = "Shopware Plugin Bootstrap.php"
        const val SHOPWARE_PLUGIN_CHANGELOG = "Shopware Plugin Changelog.md"
        const val SHOPWARE_PLUGIN_COMPOSER_JSON = "Shopware Plugin composer.json"
        const val SHOPWARE_PLUGIN_SERVICES_XML = "Shopware Plugin services.xml"

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

        fun renderTemplate(project: Project, templateName: String, properties: Map<String, String>?): String {
            return project.applyTemplate(templateName, properties)
        }

        fun applyChangelogTemplate(project: Project, config: NewChangelogConfig): String {
            return project.applyTemplate(SHOPWARE_CONTRIBUTION_CHANGELOG_TEMPLATE, config.toMap())
        }

        fun applyShopwareAdminVueComponent(project: Project, name: String, config: NewComponentConfig): String {
            val props = mapOf(
                "NAME" to config.name,
                "GENERATE_SCSS" to config.generateCss.toString(),
                "GENERATE_TWIG" to config.generateTwig.toString(),
            )

            return project.applyTemplate(name, props)
        }

        fun applyShopwareAdminVueModule(project: Project, config: NewModuleConfig): String {
            val props = mapOf(
                "NAME" to config.name,
                "TYPE" to config.type,
                "COLOR" to config.color,
                "ICON" to config.icon,
                "PARENT_MODULE" to config.parentModule,
                "VISIBLE_IN_SETTINGS" to config.showInSettings.toString(),
            )

            return project.applyTemplate(SHOPWARE_ADMIN_VUE_MODULE, props)
        }

        fun applyShopwarePluginConfig(project: Project): String {
            return project.applyTemplate(SHOPWARE_PLUGIN_CONFIG_TEMPLATE)
        }

        fun applyShopwareAdminVueModuleSnippet(project: Project, name: String): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_VUE_MODULE_SNIPPET_JSON, mapOf(
                    "NAME" to name
                )
            );
        }

        fun applyShopwareAdminVueCmsBlockIndex(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_INDEX,
                config.toMap()
            );
        }

        fun applyShopwareAdminVueCmsBlockComponentIndex(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_INDEX,
                config.toMap()
            );
        }

        fun applyShopwareAdminVueCmsBlockComponentTemplate(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_TEMPLATE,
                config.toMap()
            );
        }

        fun applyShopwareAdminVueCmsBlockPreviewIndex(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_INDEX,
                config.toMap()
            );
        }

        fun applyShopwareAdminVueCmsBlockPreviewTemplate(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_TEMPLATE,
                config.toMap()
            );
        }

        fun applyShopwareAdminVueCmsBlockPreviewScss(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_SCSS,
                config.toMap()
            );
        }

        fun applyShopwareAdminVueCmsBlockStorefront(project: Project, config: NewCmsBlockConfig): String {
            return project.applyTemplate(
                SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_STOREFRONT_TEMPLATE,
                config.toMap()
            );
        }

        fun applyShopwarePHPScheduledTask(project: Project, config: NewScheduledTaskConfig): String {
            return project.applyTemplate(
                SHOPWARE_PHP_SCHEDULED_TASK, mapOf(
                    "NAME" to config.name,
                    "TASKNAME" to config.taskName,
                    "INTERVAL" to config.interval,
                    "NAMESPACE" to config.namespace
                )
            );
        }

        fun applyShopwarePHPScheduledTaskHandler(project: Project, config: NewScheduledTaskConfig): String {
            return project.applyTemplate(
                SHOPWARE_PHP_SCHEDULED_TASK_HANDLER, mapOf(
                    "NAME" to config.name,
                    "TASKNAME" to config.taskName,
                    "INTERVAL" to config.interval,
                    "NAMESPACE" to config.namespace
                )
            );
        }

        fun applyShopwarePluginComposerJson(project: Project, config: NewPluginConfig): String {
            return project.applyTemplate(
                SHOPWARE_PLUGIN_COMPOSER_JSON, config.toMap()
            );
        }

        fun applyShopwarePluginChangelog(project: Project, config: NewPluginConfig): String {
            return project.applyTemplate(
                SHOPWARE_PLUGIN_CHANGELOG, config.toMap()
            );
        }

        fun applyShopwarePluginBootstrap(project: Project, config: NewPluginConfig): String {
            return project.applyTemplate(
                SHOPWARE_PLUGIN_BOOTSTRAP, config.toMap()
            );
        }

        fun applyShopwarePluginServicesXml(project: Project, config: NewPluginConfig): String {
            return project.applyTemplate(
                SHOPWARE_PLUGIN_SERVICES_XML, config.toMap()
            )
        }

        fun applyShopwarePHPEventListener(project: Project, config: Map<String, String>): String {
            return project.applyTemplate(
                SHOPWARE_PHP_EVENT_LISTENER, config
            )
        }
    }

    private class CustomDescriptor(fileName: String, val visibleName: String?) : FileTemplateDescriptor(fileName) {
        override fun getDisplayName(): String = visibleName ?: fileName
    }
}