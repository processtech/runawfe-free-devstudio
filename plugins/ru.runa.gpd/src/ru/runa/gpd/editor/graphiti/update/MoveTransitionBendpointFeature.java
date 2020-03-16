package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IMoveBendpointContext;
import org.eclipse.graphiti.features.impl.DefaultMoveBendpointFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.lang.model.Transition;

public class MoveTransitionBendpointFeature extends DefaultMoveBendpointFeature implements CustomUndoRedoFeature {

    private org.eclipse.draw2d.geometry.Point undoPoint;
    private org.eclipse.draw2d.geometry.Point redoPoint;

    public MoveTransitionBendpointFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean moveBendpoint(IMoveBendpointContext context) {
        if (super.moveBendpoint(context)) {
            int index = context.getBendpointIndex();
            FreeFormConnection connection = context.getConnection();
            Point point = connection.getBendpoints().get(index);
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            undoPoint = transition.getBendpoints().get(index).getCopy();
            transition.setBendpoint(index, new org.eclipse.draw2d.geometry.Point(point.getX(), point.getY()));
            return true;
        }
        return false;
    }

    @Override
    public boolean canUndo(IContext context) {
        return undoPoint != null;
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IMoveBendpointContext) {
            int index = ((IMoveBendpointContext) context).getBendpointIndex();
            FreeFormConnection connection = ((IMoveBendpointContext) context).getConnection();
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            redoPoint = transition.getBendpoints().get(index).getCopy();
            transition.setBendpoint(index, undoPoint);
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return redoPoint != null;
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IMoveBendpointContext) {
            int index = ((IMoveBendpointContext) context).getBendpointIndex();
            FreeFormConnection connection = ((IMoveBendpointContext) context).getConnection();
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            transition.setBendpoint(index, redoPoint);
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
