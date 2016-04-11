package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IAddBendpointContext;
import org.eclipse.graphiti.features.impl.DefaultAddBendpointFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;

import ru.runa.gpd.lang.model.Transition;

public class AddTransitionBendpointFeature extends DefaultAddBendpointFeature {
    public AddTransitionBendpointFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public void addBendpoint(IAddBendpointContext context) {
        super.addBendpoint(context);
        int index = context.getBendpointIndex();
        FreeFormConnection connection = context.getConnection();
        Point point = connection.getBendpoints().get(index);
        Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
        transition.addBendpoint(index, new org.eclipse.draw2d.geometry.Point(point.getX(), point.getY()));
    }
}
