package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;

public class CreateStartNodeFeature extends CreateElementFeature {
    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (parentObject instanceof Subprocess) {
            return false;
        }
        if (getProcessDefinition().getChildren(StartState.class).size() > 0) {
            return false;
        }
        return super.canCreate(context);
    }
}
