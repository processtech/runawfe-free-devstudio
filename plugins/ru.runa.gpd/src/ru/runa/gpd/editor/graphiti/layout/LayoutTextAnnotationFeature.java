package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;

public class LayoutTextAnnotationFeature extends LayoutElementFeature {
    public static final String POLYLINE = "POLYLINE";
    public static final int EDGE = 20;

    @Override
    public boolean layout(ILayoutContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        Dimension bounds = adjustBounds(context);
        Polyline polyline = PropertyUtil.findGaRecursiveByName(containerShape, POLYLINE);
        polyline.getPoints().set(2, getNewPoint(polyline, 2, bounds.height));
        polyline.getPoints().set(3, getNewPoint(polyline, 3, bounds.height));
        Graphiti.getGaService().setLocationAndSize(polyline, 0, 0, EDGE, bounds.height);
        GraphicsAlgorithm text = PropertyUtil.findGaRecursiveByName(containerShape, GaProperty.DESCRIPTION);
        Graphiti.getGaService().setLocationAndSize(text, 5, 5, bounds.width - 5, bounds.height - 5);
        return true;
    }

    private Point getNewPoint(Polyline line, int pointIndex, int height) {
        Point p = line.getPoints().get(pointIndex);
        return Graphiti.getGaService().createPoint(p.getX(), height);
    }
}
