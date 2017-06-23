package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class CreateActionFeature extends CreateElementFeature {

    @Override
    public boolean canCreate(ICreateContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        Object parentConnection = getBusinessObjectForPictogramElement(context.getTargetConnection());
        return parentObject instanceof TaskState || parentConnection instanceof Transition; 
    }

}
