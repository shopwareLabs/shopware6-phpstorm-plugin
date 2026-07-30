package de.shyim.shopware6.action.generator.app

import de.shyim.shopware6.templates.ShopwareTemplates
import icons.ShopwareToolBoxIcons

class AddCustomEntitiesAction : AddConfigFileAction(
    "entities.xml",
    "Resources",
    ShopwareTemplates.SHOPWARE_APP_CUSTOM_ENTITIES,
    "generator.app_custom_entities",
    "Add Custom Entities",
    "Add custom entities to an app",
    ShopwareToolBoxIcons.SHOPWARE
)