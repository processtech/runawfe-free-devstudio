package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.Node;

public class LayoutSubprocessNodeFeature extends LayoutStateNodeFeature {
    public static final String SECOND_BORDER_RECT = "secondBorderRect";

    @Override
    protected void doCustomLayout(Node node, ILayoutContext context, GraphicsAlgorithm container, ContainerShape containerShape) {
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();

        GraphicsAlgorithm secondBorderRect = PropertyUtil.findGaRecursiveByName(ga, SECOND_BORDER_RECT);
        if (secondBorderRect != null) {
            Graphiti.getGaService().setLocationAndSize(secondBorderRect, GRID_SIZE / 2 + 5, GRID_SIZE / 2 + 5, container.getWidth() - GRID_SIZE - 10,
                    container.getHeight() - GRID_SIZE - 10);
        }
    }

}
