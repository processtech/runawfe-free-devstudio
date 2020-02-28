package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;

public abstract class UpdateFeature extends AbstractUpdateFeature {
    private DiagramFeatureProvider featureProvider;

    public UpdateFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public DiagramFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public boolean canUpdate(IUpdateContext context) {
        return true;
    }

    @Override
    public boolean hasDoneChanges() {
        return false;
    }
}
