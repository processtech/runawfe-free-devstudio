package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;
import ru.runa.gpd.ui.dialog.RenameTransitionDialog;
import ru.runa.gpd.PluginLogger;

public class RenameItemDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        ActionContainer actionContainer = (ActionContainer) getSelectionNotNull(); 
        RenameTransitionDialog dlg = new RenameTransitionDialog();
        if (dlg.open() == RenameTransitionDialog.OK) {
            String result =  dlg.getValue().trim();
            actionContainer.setName(result);
        } else {
            PluginLogger.logInfo("Running context menu at Transition: RenameTransition failed\n");
        }
    }

}
