package de.shyim.shopware6.installer

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.ide.util.projectWizard.WebProjectTemplate
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.ProjectGeneratorPeer
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

        val envFile = File(toDir, ".env")
        envFile.createNewFile()

        val gitIgnoreFile = File(toDir, ".gitignore")
        gitIgnoreFile.writeText(".idea\n/vendor\n")

        val customPluginsDir = File(toDir, "custom/plugins")
        customPluginsDir.mkdirs()

        val customStaticPluginsDir = File(toDir, "custom/static-plugins")
        customStaticPluginsDir.mkdirs()


        val composerJson = """
        {
            "name": "shopware/production",
            "license": "MIT",
            "type": "project",
            "require": {
                "composer-runtime-api": "^2.0",
                "shopware/administration": "*",
                "shopware/core": "${settings.version.name}",
                "shopware/elasticsearch": "*",
                "shopware/storefront": "*",
                "symfony/flex": "~2"
            },
            "repositories": [
                {
                    "type": "path",
                    "url": "custom/plugins/*",
                    "options": {
                        "symlink": true
                    }
                },
                {
                    "type": "path",
                    "url": "custom/plugins/*/packages/*",
                    "options": {
                        "symlink": true
                    }
                },
                {
                    "type": "path",
                    "url": "custom/static-plugins/*",
                    "options": {
                        "symlink": true
                    }
                }
            ],
        	${settings.version.name.contains("RC").let { """"minimum-stability": "RC",""" }}
            "prefer-stable": true,
            "config": {
                "allow-plugins": {
                    "symfony/flex": true,
                    "symfony/runtime": true
                },
                "optimize-autoloader": true,
                "sort-packages": true
            },
            "scripts": {
                "auto-scripts": [
                ],
                "post-install-cmd": [
                    "@auto-scripts"
                ],
                "post-update-cmd": [
                    "@auto-scripts"
                ]
            },
            "extra": {
                "symfony": {
                    "allow-contrib": true,
                    "endpoint": [
                        "https://raw.githubusercontent.com/shopware/recipes/flex/main/index.json",
                        "flex://defaults"
                    ]
                }
            }
        }
        """.trimIndent()

        val composerJsonFile = File(toDir, "composer.json")
        composerJsonFile.writeText(composerJson)

        val task: Task.Backgroundable = object : Task.Backgroundable(project, "Installing dependencies", true) {
            override fun run(progressIndicator: ProgressIndicator) {
                try {
                    // Run command
                    val commandLine = GeneralCommandLine("composer", "install", "--no-interaction")
                    commandLine.setWorkDirectory(toDir)
                    CapturingProcessHandler(commandLine).runProcess()
                } catch (e: IOException) {
                    showErrorNotification(project, "There is a error occurred: ${e.message}")
                }
            }
        }

        ProgressManager.getInstance().run(task)
    }

    override fun createPeer(): ProjectGeneratorPeer<ShopwareProjectSettings> {
        return ShopwareProjectGeneratorPeer()
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