package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.wfe.lang.MultiTaskSynchronizationMode;

public class ChangeMultiTaskSynchronizationModeFeature extends ChangePropertyFeature<MultiTaskState, MultiTaskSynchronizationMode> {

    public ChangeMultiTaskSynchronizationModeFeature(MultiTaskState target, MultiTaskSynchronizationMode newValue) {
        super(target, target.getSynchronizationMode(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setSynchronizationMode(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setSynchronizationMode(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("MultiTask.property.synchronizationMode");
    }

}
