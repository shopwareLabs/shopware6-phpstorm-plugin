# Twig Block Versioning

When a plugin or theme overrides a Twig block, the override silently drifts apart from the template it is based on: the upstream block changes with every Shopware (or extension) update, and nothing tells you that your override needs attention. Twig Block Versioning records *which* upstream state your override is based on, so the plugin can notify you when the upstream block changes or is removed.

## The versioning comment

A versioned block carries a comment directly above it:

```twig
{% sw_extends '@Storefront/storefront/page/product-detail/index.html.twig' %}

{# shopware-block: 228011013378f9c5804c1ee52d65b257cc532d1dc981cd059cf7443b8c248211@6.7.2.0 #}
{% block page_product_detail_content %}
    {# your override #}
{% endblock %}
```

The comment consists of two parts:

- **hash** — a SHA-512 hash of the full upstream block content (from `{% block %}` to `{% endblock %}`) at the time you wrote or last reviewed your override.
- **version** — the version of the extension the upstream block belongs to, purely informational. For blocks from `vendor/` it is the installed Composer package version, for extensions in `custom/plugins` it is read from the extension's `composer.json`. If no version can be determined, the suffix is omitted.

Versioning works for blocks of the Shopware core **and any other extension** — whether it is installed via Composer (`vendor/`) or lives in `custom/plugins`.

## Workflow

1. **Add comments** — place the caret on a block and use the intention (Alt+Enter) *"Add/Update the Shopware 6 versioning comment"*. Alternatively enable the inspection *"Shopware versioning block comment is missing"* (disabled by default), which flags every override without a comment and offers a quick fix. Blocks created through the *"Extend Twig Block"* intention get the comment automatically when that inspection is enabled.
2. **Update Shopware or an extension.**
3. **Run inspections** — every override whose upstream block changed or disappeared is reported:

| Inspection | Default | Reports |
| --- | --- | --- |
| The upstream block has changed | on | The recorded hash no longer matches the upstream block — review your override, then use the intention to update the comment. |
| The upstream block has been removed | on | The block no longer exists upstream — your override is dead code or needs rework. If the block still exists in another template, the message lists where. |
| Shopware versioning block comment is missing | off | An override of an upstream block has no versioning comment yet. |

4. **Review and update** — for Shopware core blocks the intention *"Show Twig block difference"* shows a diff between the recorded version of the block (fetched from the `shopware/shopware` GitHub repository) and the current one. For third-party extensions no diff can be shown — the notification itself is the value.

Once an override is reviewed, re-run the *"Add/Update"* intention (or the quick fix) to record the new hash, which clears the warning.

## How the upstream of a block is resolved

All blocks of all templates under `Resources/views/` are indexed with their content hash. For a block in your file, the upstream candidates are determined in two steps:

1. **The `sw_extends` chain (primary).** The plugin follows the template's `{% sw_extends '@Bundle/path' %}` references recursively. Blocks found in one of these parent templates are the upstream — regardless of anything else. This also works when your template extends another template at a *different* relative path (template reuse).
2. **Same relative path (fallback).** If the block is not found in the chain — for example it was injected by another plugin's override of the same template, the file has no `sw_extends`, or the referenced bundle cannot be resolved — templates of *other* bundles at the same path relative to `Resources/views/` are considered. In this fallback, blocks that carry a versioning comment themselves are ignored: a comment declares the block to be an override of something else, so it can never be upstream. This prevents a sibling plugin that overrides the same block from masking upstream changes or removals.

When several upstream candidates remain, a hash matching **any** of them counts as up to date, and when recording a new comment the nearest chain parent wins — otherwise Shopware core, then `vendor/` extensions, then by path.

Additional safeguards:

- Upstream templates themselves (`vendor/`, `src/Storefront`) are never asked for versioning comments, and the missing-comment inspection only considers files that extend another template.
- The removed-block inspection stays silent when the Shopware sources are not part of the project (e.g. a standalone plugin repository without `vendor/`), because there is no upstream to compare against.

## Limitations

- One hash is recorded per block. If a block exists in several templates of your inheritance chain (e.g. a theme overriding a core block), a change is only reported when the content matches *none* of them.
- The runtime template chain in Shopware is determined by plugin load order, which is not statically known. The `sw_extends` references are the best static approximation; blocks provided only by a parallel override of the same template are handled by the fallback.
- A third-party plugin that still ships a removed core block *without* versioning comments can prevent the removed-block warning — in that case the changed-block inspection usually reports instead.
- The block diff is only available for Shopware core templates.
