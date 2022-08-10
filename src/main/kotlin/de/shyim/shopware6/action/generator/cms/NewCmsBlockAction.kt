package de.shyim.shopware6.action.generator.cms

import com.intellij.lang.javascript.JavaScriptFileType
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiManager
import com.jetbrains.twig.TwigFileType
import de.shyim.shopware6.action.generator.ActionUtil
import de.shyim.shopware6.templates.ShopwareTemplates
import de.shyim.shopware6.util.PsiUtil
import de.shyim.shopware6.util.ShopwareBundleUtil
import icons.ShopwareToolBoxIcons
import org.apache.commons.io.FilenameUtils
import org.jetbrains.plugins.scss.SCSSFileType
import java.nio.file.Paths

class NewCmsBlockAction :
    DumbAwareAction("Create a New CMS Block", "Create a new CMS Block in Extension", ShopwareToolBoxIcons.SHOPWARE) {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) {
            return
        }

        val ui = NewCmsBlockDialogWrapper(ShopwareBundleUtil.getAllBundles(e.project!!))
        val result = ui.showAndGetResult() ?: return

        val virtualFolder = LocalFileSystem.getInstance()
            .findFileByPath(FilenameUtils.separatorsToUnix(Paths.get(result.extension.path).parent.toString()))
        val psiFolder = PsiManager.getInstance(e.project!!).findDirectory(virtualFolder!!)

        val blockFolder = PsiUtil.createFolderRecursive(
            psiFolder!!,
            "Resources/app/administration/src/module/sw-cms/blocks/${result.group}/${result.name}"
        )

        val rootFile = ActionUtil.createFile(
            e.project!!,
            JavaScriptFileType.INSTANCE,
            "index.js",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_INDEX,
                result.toMap()
            ),
            blockFolder
        ) ?: return

        val componentFolder = PsiUtil.createFolderRecursive(blockFolder, "component")

        ActionUtil.createFile(
            e.project!!,
            JavaScriptFileType.INSTANCE,
            "index.js",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_INDEX,
                result.toMap()
            ),
            componentFolder
        ) ?: return

        ActionUtil.createFile(
            e.project!!,
            TwigFileType.INSTANCE,
            "sw-cms-block-${result.name}.html.twig",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_TEMPLATE,
                result.toMap()
            ),
            componentFolder
        ) ?: return

        val previewFolder = PsiUtil.createFolderRecursive(blockFolder, "preview")

        ActionUtil.createFile(
            e.project!!,
            JavaScriptFileType.INSTANCE,
            "index.js",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_INDEX,
                result.toMap()
            ),
            previewFolder
        ) ?: return

        ActionUtil.createFile(
            e.project!!,
            TwigFileType.INSTANCE,
            "sw-cms-preview-${result.name}.html.twig",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_TEMPLATE,
                result.toMap()
            ),
            previewFolder
        ) ?: return

        ActionUtil.createFile(
            e.project!!,
            SCSSFileType.SCSS,
            "sw-cms-preview-${result.name}.scss",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_PREVIEW_SCSS,
                result.toMap()
            ),
            previewFolder
        ) ?: return

        val storefrontFolder = PsiUtil.createFolderRecursive(psiFolder, "Resources/views/storefront/block")

        ActionUtil.createFile(
            e.project!!,
            TwigFileType.INSTANCE,
            "cms-block-${result.name}.html.twig",
            ShopwareTemplates.renderTemplate(
                e.project!!,
                ShopwareTemplates.SHOPWARE_ADMIN_CMS_BLOCK_COMPONENT_STOREFRONT_TEMPLATE,
                result.toMap()
            ),
            storefrontFolder
        ) ?: return

        val view = LangDataKeys.IDE_VIEW.getData(e.dataContext) ?: return

        view.selectElement(rootFile)
    }
}