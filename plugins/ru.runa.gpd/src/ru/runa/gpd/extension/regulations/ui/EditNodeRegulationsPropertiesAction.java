package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.Node;

public class EditNodeRegulationsPropertiesAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            Node node = getSelectionNotNull();
            EditNodeRegulationsPropertiesDialog dialog = new EditNodeRegulationsPropertiesDialog(node);
            dialog.open();
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

}
