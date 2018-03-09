package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.lang.model.bpmn.IBoundaryEvent;

public class GraphUtil {

    public static void createBoundaryEventEllipse(Diagram diagram, Shape ellipseShape, IBoundaryEvent boundaryEvent, int width, int height) {
        Ellipse ellipse = Graphiti.getGaService().createPlainEllipse(ellipseShape);
        ellipse.setFilled(Boolean.FALSE);
        ellipse.setLineStyle(LineStyle.DASH);
        ellipse.setStyle(StyleUtil.getStateNodeBoundaryEventEllipseStyle(diagram, boundaryEvent));
        Graphiti.getGaService().setLocationAndSize(ellipse, 0, 0, width, height);
        ellipseShape.setVisible(!boundaryEvent.isInterruptingBoundaryEvent());
    }
}
