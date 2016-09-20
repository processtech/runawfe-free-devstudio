package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.impl.LocationContext;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Timer;

public class AddTimerFeature extends AddNodeFeature implements GEFConstants {
    @Override
    public boolean canAdd(IAddContext context) {
        if (super.canAdd(context)) {
            return true;
        }
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        return parentObject instanceof ITimed;
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Object parent = getBusinessObjectForPictogramElement(context.getTargetContainer());
        Timer timer = (Timer) context.getNewObject();
        String imageName = timer.getTypeDefinition().getIcon();
        Dimension bounds = adjustBounds(context);
        if (parent instanceof ITimed) {
            ((LocationContext) context).setX(1);
            ((LocationContext) context).setY(((Node) parent).getConstraint().height - 2 * GRID_SIZE);
            imageName = "boundary_" + imageName;
            bounds.scale(0.5);
        }
        ContainerShape parentShape = context.getTargetContainer();
        IPeCreateService createService = Graphiti.getPeCreateService();
        IGaService gaService = Graphiti.getGaService();
        ContainerShape containerShape = createService.createContainerShape(parentShape, true);

        Image image = gaService.createImage(containerShape, "graph/" + imageName);
        image.setLineVisible(Boolean.TRUE);
        image.setLineStyle(LineStyle.DASH);
        image.setLineWidth(1);
        gaService.setLocationAndSize(image, context.getX(), context.getY(), bounds.width, bounds.height);
        link(containerShape, timer);
        createService.createChopboxAnchor(containerShape);

        Shape ellipseShape = createService.createShape(containerShape, true);
        Ellipse ellipse = gaService.createEllipse(ellipseShape);
        ellipse.setFilled(Boolean.FALSE);
        ellipse.setLineStyle(LineStyle.DASH);
        ellipse.setLineWidth(2);
        ellipse.setForeground(Graphiti.getGaService().manageColor(getDiagram(), StyleUtil.LIGHT_BLUE));
        gaService.setLocationAndSize(ellipse, 0, 0, bounds.width, bounds.height);
        ellipseShape.setVisible(!timer.isInterrupting());
        link(ellipseShape, timer);

        layoutPictogramElement(containerShape);
        return containerShape;
    }
}
