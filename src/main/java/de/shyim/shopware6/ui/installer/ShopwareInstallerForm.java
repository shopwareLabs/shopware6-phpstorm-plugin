package de.shyim.shopware6.ui.installer;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.ui.UIUtil;
import de.shyim.shopware6.installer.ShopwareApiUtil;
import de.shyim.shopware6.installer.ShopwareVersion;
import org.jdesktop.swingx.combobox.ListComboBoxModel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ShopwareInstallerForm {
    public JPanel contentPane;
    public JComboBox versionField;

    public ShopwareInstallerForm() {
        updateShopwareVersions();
    }

    private void updateShopwareVersions() {
        versionField.setModel(new ListComboBoxModel<>(new ArrayList<>()));

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final List<ShopwareVersion> versions = ShopwareApiUtil.INSTANCE.getAllVersions();
            UIUtil.invokeLaterIfNeeded(() -> versionField.setModel(new ListComboBoxModel<>(versions)));
        });
    }

}
