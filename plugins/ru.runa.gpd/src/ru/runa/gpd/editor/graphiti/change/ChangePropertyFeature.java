package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.editor.graphiti.IRedoProtected;

public abstract class ChangePropertyFeature<T, V> extends AbstractCustomFeature implements CustomUndoRedoFeature, IRedoProtected {

    protected IFeatureProvider fp;
    protected T target;
    protected V oldValue;
    protected V newValue;

    protected ChangePropertyFeature(T target, V oldValue, V newValue) {
        super(null);
        this.target = target;
        this.oldValue = oldValue;
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

    protected abstract void undo(IContext context);

    @Override
    public void postUndo(IContext context) {
        undo(context);
    }

    @Override
    public void postRedo(IContext context) {
        execute(context);
    }

}
