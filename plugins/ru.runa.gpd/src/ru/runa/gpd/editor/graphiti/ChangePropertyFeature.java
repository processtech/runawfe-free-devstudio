package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;

public abstract class ChangePropertyFeature<T, V> extends AbstractCustomFeature implements CustomUndoRedoFeature {

    protected IFeatureProvider fp;
    protected T target;
    protected V newValue;
    protected V oldValue;

    public ChangePropertyFeature(T target, V newValue) {
        super(null);
        this.target = target;
        this.newValue = newValue;
    }

    @Override
    public boolean canUndo(IContext context) {
        return target != null;
    }

    @Override
    public boolean canRedo(IContext context) {
        return target != null;
    }

    @Override
    public boolean canExecute(ICustomContext context) {
        return true;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return fp;
    }

    void setFeatureProvider(IFeatureProvider fp) {
        this.fp = fp;
    }

}
