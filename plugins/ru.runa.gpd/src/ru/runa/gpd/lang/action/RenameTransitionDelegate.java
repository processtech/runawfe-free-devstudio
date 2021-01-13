package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.dialog.RenameTransitionDialog;
import ru.runa.gpd.PluginLogger;

public class RenameTransitionDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
    	Transition currentTransition = (Transition) getSelectionNotNull(); 
        RenameTransitionDialog dlg = new RenameTransitionDialog();
        if (dlg.open() == RenameTransitionDialog.OK) {
            String result =  dlg.getValue().trim();           
            currentTransition.setName(result);
           
        } else {
            PluginLogger.logInfo("Running context menu at Transition: RenameTransition failed\n");
        }
    }

}
