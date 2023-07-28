package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateEventSubprocessFeature extends CreateElementFeature  {
    @Override
    public boolean canCreate(ICreateContext context) {
        if (context.getProperty(CONNECTION_PROPERTY) != null) {
            return false;
        }
        return super.canCreate(context);
    }
}
