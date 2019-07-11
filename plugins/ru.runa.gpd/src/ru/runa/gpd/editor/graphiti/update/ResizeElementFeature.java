package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.context.impl.ResizeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.GraphElement;

public class ResizeElementFeature extends DefaultResizeShapeFeature implements GEFConstants, ICustomUndoRedoFeature {

    private Rectangle undoConstraint;
    private Rectangle redoConstraint;

    public ResizeElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public boolean canResizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        return element != null && !element.getTypeDefinition().getGraphitiEntry().isFixedSize();
    }

    @Override
    public void resizeShape(IResizeShapeContext context) {
        Shape shape = context.getShape();
        GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
        if (context.getHeight() < 4 * GRID_SIZE) {
            ((ResizeContext) context).setHeight(4 * GRID_SIZE);
        }
        if (context.getWidth() < 4 * GRID_SIZE) {
            ((ResizeContext) context).setWidth(4 * GRID_SIZE);
        }
        undoConstraint = element.getConstraint().getCopy();
        element.setConstraint(new Rectangle(context.getX(), context.getY(), context.getWidth(), context.getHeight()));
        super.resizeShape(context);
    }

    @Override
    public boolean canUndo(IContext context) {
        return undoConstraint != null;
    }

    @Override
    public void preUndo(IContext context) {
        // TODO Auto-generated method stub
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IResizeShapeContext) {
            Shape shape = ((IResizeShapeContext) context).getShape();
            GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
            redoConstraint = element.getConstraint().getCopy();
            element.setConstraintSilent(undoConstraint);
        }

    }

    @Override
    public boolean canRedo(IContext context) {
        return redoConstraint != null;
    }

    @Override
    public void preRedo(IContext context) {
        // TODO Auto-generated method stub
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IResizeShapeContext) {
            Shape shape = ((IResizeShapeContext) context).getShape();
            GraphElement element = (GraphElement) getBusinessObjectForPictogramElement(shape);
            element.setConstraintSilent(redoConstraint);
        }
    }
}
