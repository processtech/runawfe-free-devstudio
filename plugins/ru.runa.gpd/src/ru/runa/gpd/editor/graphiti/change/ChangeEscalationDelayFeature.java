package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.Duration;

public class ChangeEscalationDelayFeature extends ChangePropertyFeature<TaskState, Duration> {

    public ChangeEscalationDelayFeature(TaskState target, Duration newValue) {
        super(target, target.getEscalationDelay(), newValue);
    }

    public ChangeEscalationDelayFeature(TaskState target, Duration oldValue, Duration newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setEscalationDelay(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setEscalationDelay(newValue);
    }

    @Override
    public String getName() {
        return Localization.getString("escalation.duration");
    }

}
