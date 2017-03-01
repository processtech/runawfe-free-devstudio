package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.features.impl.AbstractLayoutFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.model.GraphElement;

public abstract class LayoutElementFeature extends AbstractLayoutFeature implements GEFConstants {
    private DiagramFeatureProvider featureProvider;

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

    protected Dimension adjustBounds(ILayoutContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(containerShape);
        Dimension dimension = new Dimension(ga.getWidth(), ga.getHeight());
        Dimension minSize = getDefaultSize(element, context);
        if (dimension.height < minSize.height) {
            dimension.height = minSize.height;
            ga.setHeight(dimension.height);
        }
        if (dimension.width < minSize.width) {
            dimension.width = minSize.width;
            ga.setWidth(dimension.width);
        }
        element.setConstraint(new Rectangle(new Point(ga.getX(), ga.getY()), dimension));
        return dimension;
    }

    public Dimension getDefaultSize(GraphElement element, ILayoutContext context) {
        return element.getTypeDefinition().getGraphitiEntry().getDefaultSize();
    }
}
