package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;

public class ConnectionSettingsDialog extends CompactWizardDialog {
    private Button testButton;
    private Button syncButton;

    public ConnectionSettingsDialog(Wizard wizard) {
        super(wizard);
    }

    public Button getTestButton() {
        return testButton;
    }

    public Button getSyncButton() {
        return syncButton;
    }

    @Override
    protected void createButtonsForButtonBar(final Composite parent) {
        testButton = createButton(parent, 197, Localization.getString("button.test.connection"), false);
        syncButton = createButton(parent, 198, Localization.getString("button.Synchronize"), false);
        super.createButtonsForButtonBar(parent);
    }
}
