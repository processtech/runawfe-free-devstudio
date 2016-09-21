package ru.runa.gpd.lang.model.bpmn;


public enum EventNodeType {
    message, signal;

    public String getImageName(boolean isCatch, boolean boundary) {
        return (boundary ? "boundary_" : "") + (isCatch ? "catch" : "throw") + "_" + name() + ".png";
    }

    // public String getXmlElementName() {
    // return name().toLowerCase() + "EventDefinition";
    // }
    //
    // public static EventNodeType fromXmlName(String nodeName) {
    // for (EventNodeType eventNodeType : EventNodeType.values()) {
    // if (Objects.equal(eventNodeType.getXmlElementName(), nodeName)) {
    // return eventNodeType;
    // }
    // }
    // throw new RuntimeException("No eventNodeType found for " + nodeName);
    // }
}
