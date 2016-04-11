package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IRemoveBendpointContext;
import org.eclipse.graphiti.features.impl.DefaultRemoveBendpointFeature;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;

import ru.runa.gpd.lang.model.Transition;

public class RemoveTransitionBendpointFeature extends DefaultRemoveBendpointFeature {
    public RemoveTransitionBendpointFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public void removeBendpoint(IRemoveBendpointContext context) {
        super.removeBendpoint(context);
        int index = context.getBendpointIndex();
        FreeFormConnection connection = context.getConnection();
        Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(connection);
        transition.removeBendpoint(index);
    }
}
