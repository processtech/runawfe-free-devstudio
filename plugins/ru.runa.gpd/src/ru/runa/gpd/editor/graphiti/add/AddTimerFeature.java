package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.LocationContext;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Timer;

public class AddTimerFeature extends AddNodeWithImageFeature implements GEFConstants {
    @Override
    public boolean canAdd(IAddContext context) {
        if (super.canAdd(context)) {
            return true;
        }
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return Timer.isBoundaryEventInParent((GraphElement) parentObject);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        GraphElement parent = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
        Timer timer = (Timer) context.getNewObject();
        if (Timer.isBoundaryEventInParent(parent)) {
            String imageName = "boundary_" + timer.getTypeDefinition().getIcon();
            Dimension bounds = getBounds(context);
            ((LocationContext) context).setX(1);
            ((LocationContext) context).setY(((Node) parent).getConstraint().height - timer.getConstraint().height);
            ContainerShape parentShape = context.getTargetContainer();
            IPeCreateService createService = Graphiti.getPeCreateService();
            IGaService gaService = Graphiti.getGaService();
            ContainerShape containerShape = createService.createContainerShape(parentShape, true);

            Image image = gaService.createImage(containerShape, "graph/" + imageName);
            gaService.setLocationAndSize(image, context.getX(), context.getY(), bounds.width, bounds.height);

            Shape ellipseShape = createService.createShape(containerShape, false);
            Ellipse ellipse = Graphiti.getGaService().createPlainEllipse(ellipseShape);
            ellipse.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.BOUNDARY_ELLIPSE));
            ellipse.setStyle(StyleUtil.getStateNodeBoundaryEventEllipseStyle(getDiagram(), timer));
            ellipse.setLineVisible(!timer.isInterruptingBoundaryEvent());
            Graphiti.getGaService().setLocationAndSize(ellipse, 0, 0, bounds.width, bounds.height);

            link(containerShape, timer);
            createService.createChopboxAnchor(containerShape);
            layoutPictogramElement(containerShape);
            return containerShape;
        }
        return super.add(context);
    }
}
