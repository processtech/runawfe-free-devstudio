package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.util.Duration;

public class ChangeTtlDurationFeature extends ChangePropertyFeature<MessageNode, Duration> {

    public ChangeTtlDurationFeature(MessageNode target, Duration newValue) {
        super(target, target.getTtlDuration(), newValue);
    }

    public ChangeTtlDurationFeature(MessageNode target, Duration oldValue, Duration newValue) {
        super(target, oldValue, newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setTtlDuration(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setTtlDuration(newValue);
    }

}
