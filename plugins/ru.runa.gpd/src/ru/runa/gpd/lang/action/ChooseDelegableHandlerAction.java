package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;

public class ChooseDelegableHandlerAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        IDelegable iDelegable = (IDelegable) getSelection();
        ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(iDelegable.getDelegationType(), iDelegable.getDelegationClassName());
        String className = dialog.openDialog();
        if (className != null) {
            iDelegable.setDelegationClassName(className);
        }
    }
}
