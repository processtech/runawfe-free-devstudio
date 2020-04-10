package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.util.Duration;

public class ChangeTtlDurationFeature extends ChangePropertyFeature<MessageNode, Duration> {

    public ChangeTtlDurationFeature(MessageNode target, Duration newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setTtlDuration(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setTtlDuration(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getTtlDuration();
        target.setTtlDuration(newValue);
    }

}
