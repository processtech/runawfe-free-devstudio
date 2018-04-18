package ru.runa.gpd.editor.graphiti.create;

import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateAnnotationFeature extends CreateElementFeature {

    @Override
    public boolean canCreate(ICreateContext context) {
        if (context.getProperty(CONNECTION_PROPERTY) != null) {
            // Disable in node context menu
            return false;
        }
        return super.canCreate(context);
    }
}
