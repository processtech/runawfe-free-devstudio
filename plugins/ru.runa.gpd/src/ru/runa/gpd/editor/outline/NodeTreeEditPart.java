package ru.runa.gpd.editor.outline;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.jpdl.ActionContainer;

public class NodeTreeEditPart extends ElementTreeEditPart {

    public Node getNode() {
        return (Node) getModel();
    }

    @Override
    protected List<GraphElement> getModelChildren() {
        List<GraphElement> result = new ArrayList<GraphElement>();
        List<Transition> transitions = getNode().getLeavingTransitions();
        if (transitions.size() > 1) {
            result.addAll(transitions);
        }
        if (getNode() instanceof ActionContainer) {
            result.addAll(((ActionContainer) getNode()).getActions());
        }
        // if (result.isEmpty()) {
        // return super.getModelChildren();
        // }
        return result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String messageId = evt.getPropertyName();
        if (NODE_LEAVING_TRANSITION_ADDED.equals(messageId) || NODE_LEAVING_TRANSITION_REMOVED.equals(messageId)) {
            refreshChildren();
        }
    }

}
