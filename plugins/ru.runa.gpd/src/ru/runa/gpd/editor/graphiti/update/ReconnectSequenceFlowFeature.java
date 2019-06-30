package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.ICustomUndoRedoFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
import org.eclipse.graphiti.features.impl.DefaultReconnectionFeature;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class ReconnectSequenceFlowFeature extends DefaultReconnectionFeature implements ICustomUndoRedoFeature {

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
        targetSource = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (ReconnectionContext.RECONNECT_TARGET.equalsIgnoreCase(context.getReconnectType())) {
            oldTarget = transition.getTarget();
            transition.setTarget(targetSource);
            targetReconnected = true;
        }
        if (ReconnectionContext.RECONNECT_SOURCE.equalsIgnoreCase(context.getReconnectType())) {
            // target is the source side of the sequence flow
            ContainerShape sourceElement = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(transition.getSource());
            oldSource = (Node) getFeatureProvider().getBusinessObjectForPictogramElement(sourceElement);
            oldSource.removeLeavingTransition(transition);
            targetSource.addLeavingTransition(transition);
            sourceReconnected = true;
        }
    }

    @Override
    public boolean canUndo(IContext context) {
        return targetReconnected || sourceReconnected;
    }

    @Override
    public void preUndo(IContext context) {
        // TODO Auto-generated method stub
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
    public void preRedo(IContext context) {
        // TODO Auto-generated method stub
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
}
