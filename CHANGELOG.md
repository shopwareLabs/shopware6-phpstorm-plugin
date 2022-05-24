<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# shopware6-phpstorm-plugin Changelog

## [Unreleased]

## [0.0.27]
- Mark next EAP version as compatible

## [0.0.26]
- Added composer.json missing extra.labelextra.descriptionextra.manufacturerLinkextra.supportLinkextra.require.shopware/core

## [0.0.25]
- Added criteria id misused inspection
- Fixed ExtendTwigBlock when template folder is not existing
- Fixed compatibility with symfony plugin 0.23.215

## [0.0.24]
- Apps works now also on Extend this twig blockservices.Xservices.repository.searchnext-xxx/some-name

## [0.0.23]
- Support EAP 2022

## [0.0.22]

## [0.0.21]
- Fix compatibility issue with 2021.3

## [0.0.20]
- Fixed issue with cms element naming thanks to @breaker92 
- Add Create an App dialog
- Fixed deprecations

## [0.0.19]
- Added cms element action
- Added copy snippet as code in JSON file
- Fixed cms block preview scss file

## [0.0.18]
- Improve compatibility with newest EAP

## [0.0.17]
- Added new action to create a CMS block
- Fixed a bug in SynchronizeNamespaceAction where it doesn't get applied

## [0.0.16]
- Allow snippet creation using Quick Fix
- Added new action to synchronize IDE settings with existing extensions

## [0.0.15]
- Added Shopware.Mixin.getByName autocomplete
- Added CreateEventListenerIntention

## [0.0.14]
- Scans for storefront snippets as before
- The bundles in ExtendTemplate now sorted
- Added inspection for missing snippets in Administration

## [0.0.13]
- Extend twig block works now in vendor folder also ignores vendor plugins to choose
- ShopwareBundleIndex ignores plugins created for testing
- Added inspection or missing administration translation

## [0.0.12]
- increase min idea version

## [0.0.11]
- Added autocomplete to this.repositoryFactory.create('x');
- Added Intention Action "Extend this block" to easy extending a block
- Added inspection to show an error when abstract class has been wrong used in constructor

## [0.0.10]
- Fix system config autocomplete to add .config

## [0.0.9]
- Added autocomplete to twig functions theme_config and config
- Added autocomplete for Module.register labels
- Show only admin component autocomplete when the twig file is next to an index.js

## [0.0.8]
- Show all translation on goto handler
- Show admin components only in HTML context
- Remove internal variable usage

## [0.0.7]
- Added autocomplete for admin snippets
- Added autocomplete for admin components
- Added autocomplete for seoUrl, sw_include and sw_extends
- Added plugin generator

## [0.0.6]
- Added vue module generator
- Added scheduled task generator
- Added feature flag autocomplete and goto handler

## [0.0.5]
- Removed custom navigation goto and implemented into Symfony plugin
- The Symfony plugin does not show anymore missing snippets

## [0.0.4]
- Add changelog title escaping to dash

## [0.0.3]
- Moved generated files to file templates. Can be now edited in Settings
- Improved Vue component generation
- Added Changelog generation
- Added storefront snippet autocompletion

## [0.0.2]
- Added UUID live template
- Fix conflict with Shopware 5 PhpStorm Plugin
- Added PHPUnit live templates