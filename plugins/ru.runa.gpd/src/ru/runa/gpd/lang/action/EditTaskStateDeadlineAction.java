package ru.runa.gpd.lang.action;

import com.google.common.base.Objects;
import org.eclipse.jface.action.IAction;
import ru.runa.gpd.editor.graphiti.change.ChangeTaskStateDeadlineFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.ui.dialog.DurationEditDialog;
import ru.runa.gpd.util.Duration;

public class EditTaskStateDeadlineAction extends BaseModelActionDelegate {
    
    @Override
    public void run(IAction action) {
        TaskState taskState = getSelection();
        Duration oldDelay = taskState.getTimeOutDelay();
        DurationEditDialog dialog = new DurationEditDialog(taskState.getProcessDefinition(), taskState.getTimeOutDelay());
        Duration result = (Duration) dialog.openDialog();
        if (result != null && (!Objects.equal(result, oldDelay))) {
            UndoRedoUtil.executeFeature(new ChangeTaskStateDeadlineFeature(taskState, result));
        }
    }
}
