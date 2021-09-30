<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# shopware6-phpstorm-plugin Changelog

## [Unreleased]

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