package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveBendpointContext;
import org.eclipse.graphiti.features.impl.DefaultMoveBendpointFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;

import ru.runa.gpd.lang.model.Transition;

public class MoveTransitionBendpointFeature extends DefaultMoveBendpointFeature {
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
            transition.setBendpoint(index, new org.eclipse.draw2d.geometry.Point(point.getX(), point.getY()));
            return true;
        }
        return false;
    }
}
