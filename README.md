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


# No one aint time for user documentation, so here are a few tweets:
Click the links, images included!

<blockquote class="twitter-tweet"><p lang="en" dir="ltr">Adding admin component completion to <a href="https://twitter.com/hashtag/Shopware?src=hash&amp;ref_src=twsrc%5Etfw">#Shopware</a> 6 Toolbox plugin. Autocomplete for component + props working. Jumping to the component works also using STRG + CLICK. Also added feature flag autocompletion + a quick plugin generator <a href="https://t.co/7KmlAyG5UD">pic.twitter.com/7KmlAyG5UD</a></p>&mdash; Shyim (@Shyim97) <a href="https://twitter.com/Shyim97/status/1441763329115103232?ref_src=twsrc%5Etfw">September 25, 2021</a></blockquote>
<blockquote class="twitter-tweet"><p lang="en" dir="ltr">New day new <a href="https://twitter.com/hashtag/Shopware?src=hash&amp;ref_src=twsrc%5Etfw">#Shopware</a> plugin update prepared. This time theme_config and config autocomplete. Have a wish for the next feature? Create an issue with your idea :)<a href="https://t.co/Tmd66aVA5p">https://t.co/Tmd66aVA5p</a> <a href="https://t.co/bqM8jtylyt">pic.twitter.com/bqM8jtylyt</a></p>&mdash; Shyim (@Shyim97) <a href="https://twitter.com/Shyim97/status/1442952362792943617?ref_src=twsrc%5Etfw">September 28, 2021</a></blockquote>
<blockquote class="twitter-tweet"><p lang="en" dir="ltr">How to be lazier while creating <a href="https://twitter.com/hashtag/Shopware?src=hash&amp;ref_src=twsrc%5Etfw">#Shopware</a> 6 extensions. Alt+Enter on any Storefront twig block =&gt; Extend block. 😂. <a href="https://t.co/uf9hhi9Ugr">pic.twitter.com/uf9hhi9Ugr</a></p>&mdash; Shyim (@Shyim97) <a href="https://twitter.com/Shyim97/status/1443320004225687565?ref_src=twsrc%5Etfw">September 29, 2021</a></blockquote>
