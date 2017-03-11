package ru.runa.gpd.lang.model.bpmn;

public enum EventNodeType {
    message,
    signal,
    cancel,
    error;

    public String getImageName(boolean isCatch, boolean boundary) {
        return (boundary ? "boundary_" : "") + (isCatch ? "catch" : "throw") + "_" + name() + ".png";
    }

}
