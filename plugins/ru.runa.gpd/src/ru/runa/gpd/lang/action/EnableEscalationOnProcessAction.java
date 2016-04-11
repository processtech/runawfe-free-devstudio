package ru.runa.gpd.lang.action;

import java.util.List;

import org.eclipse.jface.action.IAction;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;

public class EnableEscalationOnProcessAction extends BaseModelActionDelegate {
    @Override
    public void run(IAction action) {
        List<TaskState> states = ((ProcessDefinition) getSelection()).getChildren(TaskState.class);
        for (TaskState state : states) {
            state.setUseEscalation(true);
        }
    }
}
