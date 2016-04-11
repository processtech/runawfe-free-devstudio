package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.Shape;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.editor.graphiti.add.AddElementFeature;
import ru.runa.gpd.lang.model.GraphElement;

// unused
public class ResizeStateNodeFeature extends DefaultResizeShapeFeature implements GEFConstants {
    public ResizeStateNodeFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        if (element != null) {
            IAddFeature addFeature = ((DiagramFeatureProvider) getFeatureProvider()).getAddFeature(element.getClass());
            if (addFeature instanceof AddElementFeature) {
                // return !((AddElementFeature) addFeature).isFixedSize();
                return true;
            }
        }
        return false;
    }

    @Override
    public void resizeShape(IResizeShapeContext context) {
        super.resizeShape(context);
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(context.getShape());
        Rectangle bounds = element.getConstraint().getCopy();
        bounds.x = context.getX();
        bounds.y = context.getY();
        bounds.width = context.getWidth();
        bounds.height = context.getHeight();
        element.setConstraint(bounds);
        // see ga structure in AddStateNodeFeature and hierarchy
        GraphicsAlgorithm ga = context.getShape().getGraphicsAlgorithm();
        // RoundedRectangle borderRect = (RoundedRectangle)
        // ga.getGraphicsAlgorithmChildren().get(0);
        ga.setWidth(bounds.width - GRID_SIZE);
        ga.setHeight(bounds.height - GRID_SIZE);
        GraphicsAlgorithm swimlaneText = PropertyUtil.findGaRecursiveByName(ga, GaProperty.SWIMLANE_NAME);
        if (swimlaneText != null) {
            swimlaneText.setWidth(bounds.width - GRID_SIZE);
            swimlaneText.setHeight(2 * GRID_SIZE);
        }
        GraphicsAlgorithm nameMultiText = PropertyUtil.findGaRecursiveByName(ga, GaProperty.NAME);
        if (nameMultiText != null) {
            nameMultiText.setWidth(bounds.width - GRID_SIZE);
            nameMultiText.setHeight(bounds.height - 4 * GRID_SIZE);
        }
    }
}
