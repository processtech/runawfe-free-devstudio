package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.TimerAction;

public class ChangeEscalationActionFeature extends ChangePropertyFeature<TaskState, TimerAction> {

    public ChangeEscalationActionFeature(TaskState target, TimerAction newValue) {
        super(target, target.getEscalationAction(), newValue);
    }

    public ChangeEscalationActionFeature(TaskState target, TimerAction oldValue, TimerAction newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setEscalationAction(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setEscalationAction(newValue);
    }

    @Override
    public String getName() {
        return Localization.getString("label.action.feature.escalation");
    }

}
