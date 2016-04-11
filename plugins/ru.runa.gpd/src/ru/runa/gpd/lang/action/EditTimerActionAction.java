package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.ui.dialog.TimerActionEditDialog;

public class EditTimerActionAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        Timer timer = getSelection();
        TimerActionEditDialog dialog = new TimerActionEditDialog(timer.getProcessDefinition(), timer.getAction());
        TimerAction result = (TimerAction) dialog.openDialog();
        if (result != null) {
            timer.setAction(result);
        }
    }
}
