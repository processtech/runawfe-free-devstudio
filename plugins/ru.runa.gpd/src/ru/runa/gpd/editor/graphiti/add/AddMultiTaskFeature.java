package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.GraphicsAlgorithmContainer;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.Node;

public class AddMultiTaskFeature extends AddTaskStateNodeFeature {

    @Override
    protected void addCustomGraphics(Node node, IAddContext context, GraphicsAlgorithmContainer container, ContainerShape containerShape) {
        super.addCustomGraphics(node, context, container, containerShape);
        Image image = Graphiti.getGaService().createImage(container, "graph/multiinstance.png");
        image.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.MULTIPLE_INSTANCES));
        Graphiti.getGaService().setLocation(image, node.getConstraint().width / 2 - 8, node.getConstraint().height - 2 * GRID_SIZE);
    }

}
