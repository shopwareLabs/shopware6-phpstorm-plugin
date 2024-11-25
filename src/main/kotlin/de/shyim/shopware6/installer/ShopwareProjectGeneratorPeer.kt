package de.shyim.shopware6.installer

import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.platform.ProjectGeneratorPeer
import de.shyim.shopware6.ui.installer.ShopwareInstallerForm
import javax.swing.JComponent

class ShopwareProjectGeneratorPeer : ProjectGeneratorPeer<ShopwareProjectSettings> {
    private var shopwareProjectGenerator: ShopwareInstallerForm = ShopwareInstallerForm()

    override fun getComponent(
        myLocationField: TextFieldWithBrowseButton,
        checkValid: Runnable
    ): JComponent {
        return shopwareProjectGenerator.contentPane
    }

    override fun buildUI(settingsStep: SettingsStep) {
        settingsStep.addSettingsComponent(shopwareProjectGenerator.contentPane)
    }

    override fun getSettings(): ShopwareProjectSettings {
        return ShopwareProjectSettings(this.shopwareProjectGenerator.versionField.selectedItem as ShopwareVersion)
    }

    override fun validate(): ValidationInfo? {
        return null
    }

    override fun isBackgroundJobRunning(): Boolean {
        return false
    }
}