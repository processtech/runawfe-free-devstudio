package ru.runa.gpd.algorithms.reachability;

public class ProcessFlow {
    private final String id;
    private final ProcessElement source;
    private final ProcessElement target;

    ProcessFlow(String id, ProcessElement source, ProcessElement target) {
        this.id = id;
        this.source = source;
        this.target = target;
    }

    public String getId() {
        return id;
    }

    public ProcessElement getSource() {
        return source;
    }

    public ProcessElement getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return source.getId() + " -> " + target.getId() + " (" + id + ")";
    }
}
