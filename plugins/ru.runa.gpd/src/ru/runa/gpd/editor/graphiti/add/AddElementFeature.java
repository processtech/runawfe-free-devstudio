package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddShapeFeature;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public abstract class AddElementFeature extends AbstractAddShapeFeature implements GEFConstants {
    private DiagramFeatureProvider featureProvider;

    public AddElementFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public IFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    protected ProcessDefinition getProcessDefinition() {
        return (ProcessDefinition) getBusinessObjectForPictogramElement(getDiagram());
    }

    protected Dimension getBounds(IAddContext context) {
        // TODO use LayoutFeature instead of this
        Dimension dimension = new Dimension(context.getWidth(), context.getHeight());
        return dimension;
    }

    public Dimension getDefaultSize(GraphElement element, IAddContext context) {
        return element.getTypeDefinition().getGraphitiEntry().getDefaultSize();
    }
}
