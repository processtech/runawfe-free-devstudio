package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class ReconnectSequenceFlowFeature extends DefaultReconnectionFeature {
    public ReconnectSequenceFlowFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean canReconnect(IReconnectionContext context) {
        if (!super.canReconnect(context)) {
            return false;
        }
        Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(context.getConnection());
        GraphElement graphElement = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (graphElement instanceof Node) {
            Node target = (Node) graphElement;
            if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
                return target.canReconnectArrivingTransition(transition, transition.getSource());
            }
            if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
                return target.canReconnectLeavingTransition(transition, transition.getTarget());
            }
        }
        return false;
    }

    @Override
    public void postReconnect(IReconnectionContext context) {
        Transition transition = (Transition) getFeatureProvider().getBusinessObjectForPictogramElement(context.getConnection());
        Node target = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
            transition.setTarget(target);
        }
        if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
            // target is the source side of the sequence flow
            ContainerShape sourceElement = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(transition.getSource());
            Node oldSource = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(sourceElement);
            oldSource.removeLeavingTransition(transition);
            target.addLeavingTransition(transition);
        }
    }
}
