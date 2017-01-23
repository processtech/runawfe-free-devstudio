package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.ui.dialog.EditPropertiesForRegulationDialog;

public class OpenEditPropertiesForRegulationDialogDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        try {
            Node formNode = getSelection();
            EditPropertiesForRegulationDialog dialog = new EditPropertiesForRegulationDialog(formNode);
            if (dialog.open() == IDialogConstants.OK_ID) {
                formNode.setDirty();
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}
