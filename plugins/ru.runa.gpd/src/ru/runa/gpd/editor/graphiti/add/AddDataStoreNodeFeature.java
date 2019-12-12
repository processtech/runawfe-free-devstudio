package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.layout.LayoutStateNodeFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.bpmn.DataStore;

public class AddDataStoreNodeFeature extends AddNodeFeature {
    @Override
    public PictogramElement add(IAddContext context) {
        final DataStore node = (DataStore) context.getNewObject();
        final ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);
        final Rectangle main = Graphiti.getGaService().createInvisibleRectangle(containerShape);
        Graphiti.getGaService().setLocationAndSize(main, context.getX(), context.getY(), context.getWidth(), context.getHeight());
        main.getProperties().add(new GaProperty(GaProperty.ID, LayoutStateNodeFeature.MAIN_RECT));

        Image image = Graphiti.getGaService().createImage(main, NodeRegistry.getNodeTypeDefinition(node.getClass()).getPaletteIcon());
        image.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.ICON));
        image.setTransparency(.0);
        Graphiti.getGaService().setLocationAndSize(image, -8, -8, ICON_WIDTH * 4, ICON_HEIGHT * 4);

        link(containerShape, node);

        Graphiti.getPeCreateService().createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        return containerShape;
    }
}
