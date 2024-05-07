package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Node;

public class ChangeInterruptingBoundaryEventFeature extends ChangePropertyFeature<Node, Boolean> {

    public ChangeInterruptingBoundaryEventFeature(Node target, Boolean newValue) {
        super(target, target.isInterruptingBoundaryEvent(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setInterruptingBoundaryEvent(newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setInterruptingBoundaryEvent(oldValue);
    }

    @Override
    public String getName() {
        return Localization.getString("property.interrupting");
    }

}
