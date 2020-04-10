package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;

public class ChangeTimerActionFeature extends ChangePropertyFeature<Timer, TimerAction> {

    public ChangeTimerActionFeature(Timer target, TimerAction newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setAction(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setAction(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getAction();
        target.setAction(newValue);
    }

}
