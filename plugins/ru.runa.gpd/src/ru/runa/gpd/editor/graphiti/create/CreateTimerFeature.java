package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

import ru.runa.gpd.lang.model.ITimed;

public class CreateTimerFeature extends CreateElementFeature {
    @Override
    public boolean canCreate(ICreateContext context) {
        if (super.canCreate(context)) {
            return true;
        }
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return parentObject instanceof ITimed;
    }
}
