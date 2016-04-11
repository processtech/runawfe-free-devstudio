package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.TaskState;

public class EscalationTaskFeatureDelegate extends BaseModelActionDelegate {
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        TaskState taskState = getSelection();
        if (taskState != null) {
            action.setChecked(taskState.isUseEscalation());
        }
    }

    @Override
    public void run(IAction action) {
        TaskState taskState = getSelection();
        taskState.setUseEscalation(!taskState.isUseEscalation());
    }
}
