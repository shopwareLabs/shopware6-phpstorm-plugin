# Shopware 6 Toolbox

![Build](https://github.com/shyim/shopware6-phpstorm-plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/17632.svg)](https://plugins.jetbrains.com/plugin/17632)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17632.svg)](https://plugins.jetbrains.com/plugin/17632)

<!-- Plugin description -->
Shopware 6 Toolbox is a helper plugin for Shopware 6 development. It adds some live templates and scaffolding of common Shopware files.

Current features:

- Lot of live templates for developing. Use STRG + J to see all live templates of current scope
- Generators:
  - Vue.js Admin component
  - config.xml
  - `Extend this block` in Storefront with auto file creation
  - Vue module
  - Scheduled task
  - Changelog
- Inspection to show an error when abstract class is used incorrectly in the constructor
- Autocompletion for:
  - Admin component
  - Snippets in Administration and Storefront 
  - Storefront functions theme_config, config, seoUrl, sw_include and sw_extends
  - Repositories at `this.repositoryFactory.create`
  - `Module.register` labels
  - Show only admin component autocompletion when the twig file is next to an index.js
  - Feature flag
<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "Shopware 6 Toolbox"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/shyim/shopware6-phpstorm-plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

