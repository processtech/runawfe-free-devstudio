package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;

public class InfoWithDetailsActionDialog extends InfoWithDetailsDialog {
    public InfoWithDetailsActionDialog(int dialogType, String dialogTitle, String infoMessage, String actionTitle, String details,
            boolean showDetails) {
        super(dialogType, dialogTitle, infoMessage, details, showDetails);
        this.actionTitle = actionTitle;
    }

    private String actionTitle;

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.PROCEED_ID, actionTitle, false);
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void buttonPressed(int id) {
        if (id == IDialogConstants.PROCEED_ID) {
            setReturnCode(IDialogConstants.PROCEED_ID);
            close();
        } else {
            super.buttonPressed(id);
        }
    }
}
