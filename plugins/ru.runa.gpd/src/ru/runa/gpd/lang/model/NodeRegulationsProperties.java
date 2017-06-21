package ru.runa.gpd.lang.model;

import ru.runa.gpd.lang.NodeRegistry;

public class NodeRegulationsProperties {
    private final GraphElement parent;
    private boolean enabled = true;
    private GraphElement previousNode;
    private GraphElement nextNode;
    private String descriptionForUser = "";

    public NodeRegulationsProperties(GraphElement parent) {
        this.parent = parent;
        if (NodeRegistry.hasNodeTypeDefinition(parent.getClass())) {
            setEnabled(parent.getTypeDefinition().isEnabledInRegulationsByDefault());
        }
    }

    public GraphElement getParent() {
        return parent;
    }

    public GraphElement getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(GraphElement previousNode) {
        this.previousNode = previousNode;
    }

    public GraphElement getNextNode() {
        return nextNode;
    }

    public void setNextNode(GraphElement nextNode) {
        this.nextNode = nextNode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescriptionForUser() {
        return descriptionForUser;
    }

    public void setDescriptionForUser(String descriptionForUser) {
        this.descriptionForUser = descriptionForUser;
    }

    public NodeRegulationsProperties getCopy() {
        NodeRegulationsProperties copy = new NodeRegulationsProperties(this.parent);
        copy.setPreviousNode(this.getPreviousNode());
        copy.setNextNode(this.getNextNode());
        copy.setEnabled(this.isEnabled());
        copy.setDescriptionForUser(this.getDescriptionForUser());
        return copy;
    }
}
