package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class EditTimerDurationAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        Timer timer = getSelection();
        DurationEditDialog dialog = new DurationEditDialog(timer.getProcessDefinition(), timer.getDelay());
        Duration result = (Duration) dialog.openDialog();
        if (result != null) {
            timer.setDelay(result);
        }
    }
}
