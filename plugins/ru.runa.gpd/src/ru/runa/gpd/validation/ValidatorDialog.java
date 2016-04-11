package ru.runa.gpd.validation;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;

public class ValidatorDialog extends CompactWizardDialog {

    private Button resetToDefaultsButton;

    public ValidatorDialog(Wizard wizard) {
        super(wizard);
    }

    public Button getResetToDefaultsButton() {
        return resetToDefaultsButton;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        resetToDefaultsButton = createButton(parent, 197, Localization.getString("button.default"), false);
        super.createButtonsForButtonBar(parent);
    }

}
