package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.util.Duration;

public class ChangeTaskStateDeadlineFeature extends ChangePropertyFeature<TaskState, Duration> {

    protected ChangeTaskStateDeadlineFeature(TaskState target, Duration oldValue, Duration newValue) {
        super(target, oldValue, newValue);
    }

    public ChangeTaskStateDeadlineFeature(TaskState target, Duration newValue) {
        this(target, target.getTimeOutDelay(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setTimeOutDelay(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setTimeOutDelay(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("property.deadline");
    }

}
