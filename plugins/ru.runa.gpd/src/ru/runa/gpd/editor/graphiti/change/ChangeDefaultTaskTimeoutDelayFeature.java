package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.util.Duration;

public class ChangeDefaultTaskTimeoutDelayFeature extends ChangePropertyFeature<ProcessDefinition, Duration> {

    public ChangeDefaultTaskTimeoutDelayFeature(ProcessDefinition target, Duration newValue) {
        super(target, target.getDefaultTaskTimeoutDelay(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDefaultTaskTimeoutDelay(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setDefaultTaskTimeoutDelay(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("default.task.deadline");
    }

}
