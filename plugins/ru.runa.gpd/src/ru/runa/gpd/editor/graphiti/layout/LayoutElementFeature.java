package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.IDoneChangesFeature;

public abstract class LayoutElementFeature extends AbstractLayoutFeature implements GEFConstants, IDoneChangesFeature {
    private DiagramFeatureProvider featureProvider;
    private boolean hasDoneChanges = true;

    public LayoutElementFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public boolean canLayout(ILayoutContext context) {
        return true;
    }

    @Override
    public boolean hasDoneChanges() {
        return this.hasDoneChanges;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void setHasDoneChanges(boolean hasDoneChanges) {
        this.hasDoneChanges = hasDoneChanges;
    }

}
