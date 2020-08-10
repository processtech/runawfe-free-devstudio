package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddBendpointContext;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.impl.DefaultAddBendpointFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Transition;

public class AddTransitionBendpointFeature extends DefaultAddBendpointFeature implements CustomUndoRedoFeature {

    private boolean canUndo = false;
    private org.eclipse.draw2d.geometry.Point redoBendpoint;

    public AddTransitionBendpointFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public void addBendpoint(IAddBendpointContext context) {
        super.addBendpoint(context);
        int index = context.getBendpointIndex();
        FreeFormConnection connection = context.getConnection();
        Point point = connection.getBendpoints().get(index);
        AbstractTransition transition = (AbstractTransition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
        transition.addBendpoint(index, new org.eclipse.draw2d.geometry.Point(point.getX(), point.getY()));
        canUndo = true;
    }

    @Override
    public boolean canUndo(IContext context) {
        return canUndo;
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IAddBendpointContext) {
            FreeFormConnection connection = ((IAddBendpointContext) context).getConnection();
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            int index = ((IAddBendpointContext) context).getBendpointIndex();
            redoBendpoint = transition.getBendpoints().get(index).getCopy();
            transition.removeBendpoint(index);
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return redoBendpoint != null;
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IAddBendpointContext) {
            int index = ((IAddBendpointContext) context).getBendpointIndex();
            FreeFormConnection connection = ((IAddBendpointContext) context).getConnection();
            Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            transition.addBendpoint(index, redoBendpoint);
        }
    }
}
