package de.shyim.shopware6.installer

import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
import com.intellij.util.io.Decompressor
import com.jetbrains.php.util.PhpConfigurationUtil
import fr.adrienbrault.idea.symfony2plugin.util.IdeHelper
import icons.ShopwareToolBoxIcons
import java.io.File
import java.io.IOException
import javax.swing.Icon

/**
 * @link https://github.com/Haehnchen/idea-php-shopware-plugin/blob/master/src/main/java/de/espend/idea/shopware/installer/project/ShopwareInstallerProjectGenerator.java
 */
class ShopwareProjectGenerator : WebProjectTemplate<ShopwareProjectSettings>() {
    override fun getDescription(): String {
        return "Create a Shopware 6 project"
    }

    override fun getName(): String {
        return "Shopware 6"
    }

    override fun getIcon(): Icon {
        return ShopwareToolBoxIcons.SHOPWARE
    }

    override fun generateProject(
        project: Project,
        baseDir: VirtualFile,
        settings: ShopwareProjectSettings,
        module: Module
    ) {
        val toDir = baseDir.path

        val zipFile = PhpConfigurationUtil.downloadFile(project, null, toDir, settings.version.link, "shopware.zip")

        if (zipFile == null) {
            showErrorNotification(project, "Cannot download shopware.zip file")
            return
        }

        val zip: File = VfsUtil.virtualToIoFile(zipFile)
        val base: File = VfsUtil.virtualToIoFile(baseDir)

        val task: Task.Backgroundable = object : Task.Backgroundable(project, "Extracting", true) {
            override fun run(progressIndicator: ProgressIndicator) {
                try {
                    // unzip file
                    Decompressor.Zip(zip).extract(base)

                    // Delete TMP File
                    FileUtil.delete(zip)

                    // Activate Plugin
                    IdeHelper.enablePluginAndConfigure(project)
                } catch (e: IOException) {
                    showErrorNotification(project, "There is a error occurred")
                }
            }
        }

        ProgressManager.getInstance().run(task)
    }

    override fun createPeer(): ProjectGeneratorPeer<ShopwareProjectSettings> {
        return ShopwareProjectGeneratorPeer()
    }

    override fun isPrimaryGenerator(): Boolean {
        return true
    }

    private fun showErrorNotification(project: Project, content: String) {
        Notifications.Bus.notify(
            Notification(
                "Shopware",
                "Shopware-Installer",
                content,
                NotificationType.ERROR
            ), project
        )
    }
}