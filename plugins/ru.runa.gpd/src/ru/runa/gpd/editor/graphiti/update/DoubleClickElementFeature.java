package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;

public abstract class DoubleClickElementFeature extends AbstractCustomFeature {

    protected IFeatureProvider fp;

    public DoubleClickElementFeature() {
        super(null);
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return fp;
    }

    public void setFeatureProvider(IFeatureProvider fp) {
        this.fp = fp;
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        return fp != null;
    }

    @Override
    public boolean canUndo(IContext context) {
        return false;
    }

    @Override
    public boolean hasDoneChanges() {
        return false;
    }

    protected Object getBusinessObject(ICustomContext context) {
        if (context.getPictogramElements().length != 1) {
            return null;
        }
        return fp.getBusinessObjectForPictogramElement(context.getPictogramElements()[0]);
    }

}
