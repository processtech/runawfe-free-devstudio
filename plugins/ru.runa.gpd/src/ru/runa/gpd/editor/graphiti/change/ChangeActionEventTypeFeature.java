package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ActionImpl;

public class ChangeActionEventTypeFeature extends ChangePropertyFeature<ActionImpl, String> {

    public ChangeActionEventTypeFeature(ActionImpl target, String newValue) {
        super(target, target.getEventType(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setEventType(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setEventType(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("property.eventType");
    }

}
