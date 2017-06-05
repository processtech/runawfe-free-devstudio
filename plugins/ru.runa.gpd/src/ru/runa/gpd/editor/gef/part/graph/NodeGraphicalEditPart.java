package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;

import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.policy.NodeComponentEditPolicy;
import ru.runa.gpd.editor.gef.policy.NodeGraphicalNodeEditPolicy;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class NodeGraphicalEditPart extends ElementGraphicalEditPart implements NodeEditPart {
    @Override
    public Node getModel() {
        return (Node) super.getModel();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new NodeComponentEditPolicy());
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new NodeGraphicalNodeEditPolicy());
    }

    @Override
    public NodeFigure getFigure() {
        return (NodeFigure) super.getFigure();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart editPart) {
        return getFigure().getLeavingConnectionAnchor();
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart editPart) {
        return getFigure().getArrivingConnectionAnchor();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return getFigure().getLeavingConnectionAnchor();
    }

    @Override
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return getFigure().getArrivingConnectionAnchor();
    }

    @Override
    protected List<Transition> getModelSourceConnections() {
        return getModel().getLeavingTransitions();
    }

    @Override
    protected List<Transition> getModelTargetConnections() {
        return getModel().getArrivingTransitions();
    }

    @Override
    protected void refreshVisuals() {
        getFigure().setBounds(getModel().getConstraint());
        getFigure().revalidate();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);
        String messageId = event.getPropertyName();
        if (NODE_ARRIVING_TRANSITION_ADDED.equals(messageId) || NODE_ARRIVING_TRANSITION_REMOVED.equals(messageId)) {
            refreshTargetConnections();
        } else if (NODE_LEAVING_TRANSITION_ADDED.equals(messageId) || NODE_LEAVING_TRANSITION_REMOVED.equals(messageId)
                || PROPERTY_CONFIGURATION.equals(messageId)) {
            refreshSourceConnections();
        } else if (NODE_BOUNDS_RESIZED.equals(messageId)) {
            refreshVisuals();
        } else if (PROPERTY_CHILDREN_CHANGED.equals(messageId)) {
            refreshChildren();
        } else if (PROPERTY_MINIMAZED_VIEW.equals(messageId)) {
            refreshVisuals();
        } else if (PROPERTY_INTERRUPTING_BOUNDARY_EVENT.equals(messageId)) {
            refreshVisuals();
        }
    }
}
