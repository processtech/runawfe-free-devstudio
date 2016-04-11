package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;

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
}
