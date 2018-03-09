package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.LocationContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.GraphUtil;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;

public class AddCatchEventNodeFeature extends AddEventNodeFeature implements GEFConstants {

    @Override
    public boolean canAdd(IAddContext context) {
        if (super.canAdd(context)) {
            return true;
        }
        
        GraphElement container = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
        GraphElement containerParent = container.getParent();
        return container instanceof IBoundaryEventContainer && !(containerParent instanceof IBoundaryEventContainer);
    }

    @Override
    public PictogramElement add(IAddContext context) {
    	GraphElement container = (GraphElement) getBusinessObjectForPictogramElement(context.getTargetContainer());
        GraphElement containerParent = container.getParent();
        if (container instanceof IBoundaryEventContainer && !(containerParent instanceof IBoundaryEventContainer)) {
            CatchEventNode catchEventNode = (CatchEventNode) context.getNewObject();
            Dimension bounds = getBounds(context);
            ((LocationContext) context).setX(((Node) container).getConstraint().width - 2 * GRID_SIZE);
            ((LocationContext) context).setY(((Node) container).getConstraint().height - 2 * GRID_SIZE);
            bounds.scale(0.5);
            ContainerShape parentShape = context.getTargetContainer();
            IPeCreateService createService = Graphiti.getPeCreateService();
            IGaService gaService = Graphiti.getGaService();
            ContainerShape containerShape = createService.createContainerShape(parentShape, true);

            String imageId = "graph/" + catchEventNode.getEventNodeType().getImageName(true, true);
            Image image = gaService.createImage(containerShape, imageId);
            gaService.setLocationAndSize(image, context.getX(), context.getY(), bounds.width, bounds.height);

            Shape ellipseShape = createService.createShape(containerShape, false);
            GraphUtil.createBoundaryEventEllipse(getDiagram(), ellipseShape, catchEventNode, bounds.width, bounds.height);

            link(containerShape, catchEventNode);
            createService.createChopboxAnchor(containerShape);
            layoutPictogramElement(containerShape);
            return containerShape;
        }
        return super.add(context);
    }

}
