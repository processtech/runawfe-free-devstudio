package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ConnectableViaDottedTransition;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;

public class ReconnectSequenceFlowFeature extends DefaultReconnectionFeature implements CustomUndoRedoFeature {

    private boolean sourceReconnected = false;
    private boolean targetReconnected = false;
    private boolean redoAllowed = false;
    private Node oldSource;
    private Node oldTarget;
    private Node targetSource;

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
        targetSource = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetPictogramElement());

        if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
            oldTarget = abstractTransition.getTarget();
            if (abstractTransition instanceof DottedTransition && targetSource instanceof ConnectableViaDottedTransition) {
                final ConnectableViaDottedTransition newTarget = (ConnectableViaDottedTransition) targetSource;
                final DottedTransition transition = (DottedTransition) abstractTransition;
                ((ConnectableViaDottedTransition) oldTarget).removeArrivingDottedTransition(transition);
                newTarget.addArrivingDottedTransition(transition);
            } else {
                abstractTransition.setTarget(targetSource);
            }
            targetReconnected = true;
        }
        if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
            // target is the source side of the sequence flow
            final ContainerShape sourceElement = (ContainerShape) getFeatureProvider()
                    .getPictogramElementForBusinessObject(abstractTransition.getSource());
            oldSource = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(sourceElement);
            if (targetSource instanceof ConnectableViaDottedTransition && abstractTransition instanceof DottedTransition) {
                final DottedTransition transition = (DottedTransition) abstractTransition;
                ((ConnectableViaDottedTransition) oldSource).removeLeavingDottedTransition(transition);
                ((ConnectableViaDottedTransition) targetSource).addLeavingDottedTransition(transition);
            } else {
                final Transition transition = (Transition) abstractTransition;
                oldSource.removeLeavingTransition(transition);
                targetSource.addLeavingTransition(transition);
            }
            sourceReconnected = true;
        }
    }

    @Override
    public boolean canUndo(IContext context) {
        return targetReconnected || sourceReconnected;
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IReconnectionContext) {
            Transition transition = (Transition) getFeatureProvider()
                    .getBusinessObjectForPictogramElement(((IReconnectionContext) context).getConnection());
            if (targetReconnected && oldTarget != null) {
                transition.setTarget(oldTarget);
                redoAllowed = true;
            }
            if (sourceReconnected && oldSource != null && targetSource != null) {
                targetSource.removeLeavingTransition(transition);
                oldSource.addLeavingTransition(transition);
                redoAllowed = true;
            }
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return redoAllowed;
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IReconnectionContext) {
            Transition transition = (Transition) getFeatureProvider()
                    .getBusinessObjectForPictogramElement(((IReconnectionContext) context).getConnection());
            if (targetReconnected && oldTarget != null) {
                transition.setTarget(targetSource);
            }
            if (sourceReconnected && oldSource != null && targetSource != null) {
                oldSource.removeLeavingTransition(transition);
                targetSource.addLeavingTransition(transition);
            }
        }
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}
