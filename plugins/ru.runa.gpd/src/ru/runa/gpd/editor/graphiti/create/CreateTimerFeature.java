package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;

public class CreateTimerFeature extends CreateElementFeature {
    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return (parentObject instanceof ProcessDefinition || parentObject instanceof Swimlane || parentObject instanceof ITimed);
    }
}
