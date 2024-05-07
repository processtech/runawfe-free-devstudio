package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.EventNodeType;

public class ChangeEventTypeFeature extends ChangePropertyFeature<AbstractEventNode, EventNodeType> {

    public ChangeEventTypeFeature(AbstractEventNode target, EventNodeType newValue) {
        super(target, target.getEventNodeType(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setEventNodeType(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setEventNodeType(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("property.eventType");
    }

}
