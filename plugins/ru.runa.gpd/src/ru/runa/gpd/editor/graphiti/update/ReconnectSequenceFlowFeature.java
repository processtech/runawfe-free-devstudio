package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ConnectableViaDottedTransition;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;

public class ReconnectSequenceFlowFeature extends DefaultReconnectionFeature {
    public ReconnectSequenceFlowFeature(IFeatureProvider fp) {
        super(fp);
    }

    @Override
    public boolean canReconnect(IReconnectionContext context) {
        if (!super.canReconnect(context)) {
            return false;
        }
        final AbstractTransition abstractTransition = (AbstractTransition) getFeatureProvider()
                .getBusinessObjectForPictogramElement(context.getConnection());
        final GraphElement graphElement = (GraphElement) getFeatureProvider()
                .getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (graphElement instanceof Node && abstractTransition instanceof Transition) {
            final Node target = (Node) graphElement;
            final Transition transition = (Transition) abstractTransition;
            if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
                return target.canReconnectArrivingTransition(transition, transition.getSource());
            }
            if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
                return target.canReconnectLeavingTransition(transition, transition.getTarget());
            }
        }

        if (graphElement instanceof ConnectableViaDottedTransition && abstractTransition instanceof DottedTransition) {
            final ConnectableViaDottedTransition target = (ConnectableViaDottedTransition) graphElement;
            final DottedTransition transition = (DottedTransition) abstractTransition;
            if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
                return target.canAddArrivingDottedTransition((ConnectableViaDottedTransition) transition.getSource());
            }
            if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
                return target.canReconnectLeavingDottedTransition((ConnectableViaDottedTransition) transition.getTarget());
            }
        }
        return false;
    }

    @Override
    public void postReconnect(IReconnectionContext context) {
        final AbstractTransition abstractTransition = (AbstractTransition) getFeatureProvider()
                .getBusinessObjectForPictogramElement(context.getConnection());
        final Node target = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetPictogramElement());

        if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
            if (abstractTransition instanceof DottedTransition && target instanceof ConnectableViaDottedTransition) {
                final ConnectableViaDottedTransition newTarget = (ConnectableViaDottedTransition) target;
                final DottedTransition transition = (DottedTransition) abstractTransition;
                final ConnectableViaDottedTransition oldTarget = (ConnectableViaDottedTransition) abstractTransition.getTarget();
                oldTarget.removeArrivingDottedTransition(transition);
                newTarget.addArrivingDottedTransition(transition);
            } else {
                abstractTransition.setTarget(target);
            }
        }

        if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
            // target is the source side of the sequence flow
            if (target instanceof ConnectableViaDottedTransition && abstractTransition instanceof DottedTransition) {
                final ConnectableViaDottedTransition newTarget = (ConnectableViaDottedTransition) target;
                final DottedTransition transition = (DottedTransition) abstractTransition;
                final ContainerShape sourceElement = (ContainerShape) getFeatureProvider()
                        .getPictogramElementForBusinessObject(transition.getSource());
                final ConnectableViaDottedTransition oldSource = (ConnectableViaDottedTransition) getFeatureProvider()
                        .getBusinessObjectForPictogramElement(sourceElement);

                oldSource.removeLeavingDottedTransition(transition);
                newTarget.addLeavingDottedTransition(transition);
            } else {
                final Transition transition = (Transition) abstractTransition;
                final ContainerShape sourceElement = (ContainerShape) getFeatureProvider()
                        .getPictogramElementForBusinessObject(transition.getSource());
                final Node oldSource = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(sourceElement);
                oldSource.removeLeavingTransition(transition);
                target.addLeavingTransition(transition);
            }
        }
    }
}
