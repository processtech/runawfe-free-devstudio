package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.lang.model.bpmn.IBoundaryEvent;

public class GraphUtil {

    public static Anchor getChopboxAnchor(PictogramElement pe) {
        if (pe instanceof AnchorContainer) {
            Anchor anchor = Graphiti.getPeService().getChopboxAnchor((AnchorContainer) pe);
            if (anchor != null) {
                return anchor;
            }
        }
        if (pe instanceof ContainerShape) {
            for (Shape shape : ((ContainerShape) pe).getChildren()) {
                Anchor anchor = getChopboxAnchor(shape);
                if (anchor != null) {
                    return anchor;
                }
            }
        }
        return null;
    }

    public static void createBoundaryEventEllipse(Diagram diagram, Shape ellipseShape, IBoundaryEvent boundaryEvent, int width, int height) {
        Ellipse ellipse = Graphiti.getGaService().createEllipse(ellipseShape);
        ellipse.setFilled(Boolean.FALSE);
        ellipse.setLineStyle(LineStyle.DASH);
        ellipse.setLineWidth(2);
        ellipse.setForeground(Graphiti.getGaService().manageColor(diagram, StyleUtil.LIGHT_BLUE));
        Graphiti.getGaService().setLocationAndSize(ellipse, 0, 0, width, height);
        ellipseShape.setVisible(!boundaryEvent.isInterruptingBoundaryEvent());
    }
}
