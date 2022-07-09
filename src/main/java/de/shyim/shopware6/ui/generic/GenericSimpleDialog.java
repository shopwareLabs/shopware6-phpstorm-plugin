package de.shyim.shopware6.ui.generic;

import javax.swing.*;

public class GenericSimpleDialog extends JDialog {
    public JPanel contentPane;
    public JTextField fileName;

    public GenericSimpleDialog() {
        setContentPane(contentPane);
        setModal(true);
    }
}
