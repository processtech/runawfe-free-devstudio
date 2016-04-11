package ru.runa.gpd.editor.gef.part.tree;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.lang.model.Active;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;

public class NodeTreeEditPart extends ElementTreeEditPart {

    public Node getNode() {
        return (Node) getModel();
    }

    @Override
    protected List<GraphElement> getModelChildren() {
        List<GraphElement> result = new ArrayList<GraphElement>();
        result.addAll(getNode().getLeavingTransitions());
        if (getNode() instanceof Active) {
            result.addAll(((Active) getNode()).getActions());
        }
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
