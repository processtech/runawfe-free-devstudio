package ru.runa.gpd.lang.model;

public class NodeRegulationsProperties {
    private GraphElement previousNode;
    private GraphElement nextNode;
    private boolean isEnabled;
    private String descriptionForUser;

    public NodeRegulationsProperties() {
        previousNode = null;
        nextNode = null;
        isEnabled = true;
        descriptionForUser = "";
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

    public boolean getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getDescriptionForUser() {
        return descriptionForUser;
    }

    public void setDescriptionForUser(String descriptionForUser) {
        this.descriptionForUser = descriptionForUser;
    }
}
