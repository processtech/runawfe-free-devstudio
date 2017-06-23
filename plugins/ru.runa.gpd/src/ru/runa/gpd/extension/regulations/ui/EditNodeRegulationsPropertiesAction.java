package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.settings.CommonPreferencePage;

public class EditNodeRegulationsPropertiesAction extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        try {
            Node node = getSelection();
            EditNodeRegulationsPropertiesDialog dialog = new EditNodeRegulationsPropertiesDialog(node);
            dialog.open();
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        action.setEnabled(getSelection() != null && CommonPreferencePage.isRegulationsMenuItemsEnabled());
    }

}
