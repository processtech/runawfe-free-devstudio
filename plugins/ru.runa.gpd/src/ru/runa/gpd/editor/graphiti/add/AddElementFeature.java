package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
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

    protected Dimension adjustBounds(IAddContext context) {
        Dimension dimension = new Dimension(context.getWidth(), context.getHeight());
        GraphElement element = (GraphElement) context.getNewObject();
        Dimension min = getDefaultSize(element, context);
        if (dimension.height < min.height) {
            dimension.height = min.height;
        }
        if (dimension.width < min.width) {
            dimension.width = min.width;
        }
        element.setConstraint(new Rectangle(new Point(context.getX(), context.getY()), dimension));
        return dimension;
    }

    public Dimension getDefaultSize(GraphElement element, IAddContext context) {
        return element.getTypeDefinition().getGraphitiEntry().getDefaultSize();
    }
}
