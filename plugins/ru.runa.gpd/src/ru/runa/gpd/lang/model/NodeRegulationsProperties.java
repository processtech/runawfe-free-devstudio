package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

public class NodeRegulationsProperties {
    private GraphElement previousNode;
    private GraphElement nextNode;
    private boolean isEnabled;
    private String descriptionForUser;
    private final GraphElement parent;

    public NodeRegulationsProperties(GraphElement parent) {
        previousNode = null;
        nextNode = null;
        isEnabled = true;
        descriptionForUser = "";
        this.parent = parent;
    }

    public GraphElement getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(GraphElement previousNode) {
        String oldPreviousNodeLabel = "";
        if (this.previousNode != null) {
            oldPreviousNodeLabel = this.previousNode.getLabel();
        }
        this.previousNode = previousNode;
        if (previousNode != null) {
            parent.firePropertyChange(PropertyNames.PROPERTY_PREVIOUS_NODE_IN_REGULATIONS, oldPreviousNodeLabel, this.previousNode.getLabel());
        } else {
            parent.firePropertyChange(PropertyNames.PROPERTY_PREVIOUS_NODE_IN_REGULATIONS, oldPreviousNodeLabel,
                    Localization.getString("Node.property.previousNodeInRegulations.notSet"));
        }
    }

    public GraphElement getNextNode() {
        return nextNode;
    }

    public void setNextNode(GraphElement nextNode) {
        String oldNextNodeLabel = "";
        if (this.nextNode != null) {
            oldNextNodeLabel = this.nextNode.getLabel();
        }

        this.nextNode = nextNode;
        if (nextNode != null) {
            parent.firePropertyChange(PropertyNames.PROPERTY_NEXT_NODE_IN_REGULATIONS, oldNextNodeLabel, this.nextNode.getLabel());
        } else {
            parent.firePropertyChange(PropertyNames.PROPERTY_NEXT_NODE_IN_REGULATIONS, oldNextNodeLabel,
                    Localization.getString("Node.property.nextNodeInRegulations.notSet"));

        }
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        String oldIsEnabled = String.valueOf(this.getIsEnabled());
        this.isEnabled = isEnabled;
        parent.firePropertyChange(PropertyNames.PROPERTY_NODE_INCLUDE_IN_REGULATIONS, oldIsEnabled, this.getIsEnabled());
    }

    public String getDescriptionForUser() {
        return descriptionForUser;
    }

    public void setDescriptionForUser(String descriptionForUser) {
        this.descriptionForUser = descriptionForUser;
    }
}
