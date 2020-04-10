package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.Duration;

public class ChangeTimerDelayFeature extends ChangePropertyFeature<Timer, Duration> {

    public ChangeTimerDelayFeature(Timer target, Duration newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setDelay(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setDelay(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getDelay();
        target.setDelay(newValue);
    }

}
