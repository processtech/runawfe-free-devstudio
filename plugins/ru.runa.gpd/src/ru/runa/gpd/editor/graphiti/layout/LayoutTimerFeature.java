package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Timer;

public class LayoutTimerFeature extends LayoutElementFeature {

    @Override
    public boolean layout(ILayoutContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();

        Timer node = (Timer) getBusinessObjectForPictogramElement(containerShape);
        if (node.isBoundaryEvent()) {
            Rectangle parentRectangle = ((Node) node.getParent()).getConstraint();
            Graphiti.getGaService().setLocation(ga, 1, parentRectangle.height - node.getConstraint().height);
        }
        return true;
    }
}
