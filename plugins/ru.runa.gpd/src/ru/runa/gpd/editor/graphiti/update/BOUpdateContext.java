package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class BOUpdateContext extends UpdateContext {
    private Object model;

    public BOUpdateContext(PictogramElement pictogramElement, Object model) {
        super(pictogramElement);
        this.model = model;
    }

    public Object getModel() {
        return model;
    }
}
