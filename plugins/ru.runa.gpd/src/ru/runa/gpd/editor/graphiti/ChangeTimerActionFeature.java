package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;

public class ChangeTimerActionFeature extends ChangePropertyFeature<Timer, TimerAction> {

    public ChangeTimerActionFeature(Timer target, TimerAction newValue) {
        super(target, target.getAction(), newValue);
    }

    public ChangeTimerActionFeature(Timer target, TimerAction oldValue, TimerAction newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setAction(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setAction(newValue);
    }

}
