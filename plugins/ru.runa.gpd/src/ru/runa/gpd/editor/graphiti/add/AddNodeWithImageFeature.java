package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.lang.model.Node;

public class AddNodeWithImageFeature extends AddNodeFeature {

    @Override
    public PictogramElement add(IAddContext context) {
        Node node = (Node) context.getNewObject();
        Dimension bounds = getBounds(context);
        ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);
        Image image = Graphiti.getGaService().createImage(containerShape, "graph/" + getIcon(node));
        Graphiti.getGaService().setLocationAndSize(image, context.getX(), context.getY(), bounds.width, bounds.height);
        link(containerShape, node);
        Graphiti.getPeCreateService().createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        return containerShape;
    }

    protected String getIcon(Node node) {
        return node.getTypeDefinition().getIcon();
    }

}
