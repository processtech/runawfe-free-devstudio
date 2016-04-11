package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class EditTaskStateDeadlineAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        TaskState taskState = getSelection();
        DurationEditDialog dialog = new DurationEditDialog(taskState.getProcessDefinition(), taskState.getTimeOutDelay());
        Duration result = (Duration) dialog.openDialog();
        if (result != null) {
            taskState.setTimeOutDelay(result);
        }
    }
}
