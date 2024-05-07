package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.TaskState;

public class ChangeReassignSwimlaneToInitializerFeature extends ChangePropertyFeature<TaskState, Boolean> {

    public ChangeReassignSwimlaneToInitializerFeature(TaskState target, Boolean newValue) {
        super(target, target.isReassignSwimlaneToInitializer(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setReassignSwimlaneToInitializer(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setReassignSwimlaneToInitializer(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("Swimlane.reassignSwimlaneToInitializer");
    }

}
