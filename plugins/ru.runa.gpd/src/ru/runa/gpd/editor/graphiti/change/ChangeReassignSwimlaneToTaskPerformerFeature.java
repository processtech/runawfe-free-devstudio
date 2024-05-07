package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.FormNode;

public class ChangeReassignSwimlaneToTaskPerformerFeature extends ChangePropertyFeature<FormNode, Boolean> {

    public ChangeReassignSwimlaneToTaskPerformerFeature(FormNode target, Boolean newValue) {
        super(target, target.isReassignSwimlaneToTaskPerformer(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setReassignSwimlaneToTaskPerformer(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setReassignSwimlaneToTaskPerformer(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer");
    }

}
