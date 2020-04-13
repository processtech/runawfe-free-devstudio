package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.Duration;

public class ChangeTimerDelayFeature extends ChangePropertyFeature<Timer, Duration> {

    public ChangeTimerDelayFeature(Timer target, Duration newValue) {
        super(target, target.getDelay(), newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setDelay(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDelay(newValue);
    }

}
