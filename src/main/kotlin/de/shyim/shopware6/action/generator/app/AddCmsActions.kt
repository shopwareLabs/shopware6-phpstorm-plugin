package de.shyim.shopware6.action.generator.app

import de.shyim.shopware6.templates.ShopwareTemplates
import icons.ShopwareToolBoxIcons

class AddCmsActions : AddConfigFileAction(
    "cms.xml",
    "Resources",
    ShopwareTemplates.SHOPWARE_APP_CMS,
    "Add CMS blocks",
    "Add CMS blocks to an app",
    ShopwareToolBoxIcons.SHOPWARE
)