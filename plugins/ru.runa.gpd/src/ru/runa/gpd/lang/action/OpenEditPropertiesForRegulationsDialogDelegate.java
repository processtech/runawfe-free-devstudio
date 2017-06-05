package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.dialog.EditPropertiesForRegulationsDialog;

public class OpenEditPropertiesForRegulationsDialogDelegate extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        try {
            Node formNode = getSelection();
            EditPropertiesForRegulationsDialog dialog = new EditPropertiesForRegulationsDialog(formNode);
            dialog.open();
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (getSelection() != null && CommonPreferencePage.isRegulationsMenuItemsEnabled()) {
            action.setEnabled(true);
        } else {
            action.setEnabled(false);
        }
    }

}
