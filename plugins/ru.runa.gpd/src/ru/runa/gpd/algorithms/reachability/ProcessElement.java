package ru.runa.gpd.algorithms.reachability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessElement {
    private final String id;
    private final String name;
    private final ElementType type;
    private final ProcessElement host;
    private final boolean interrupting;
    private final List<ProcessFlow> arrivingFlows = new ArrayList<>();
    private final List<ProcessFlow> leavingFlows = new ArrayList<>();
    private final List<ProcessElement> boundaryEvents = new ArrayList<>();

    ProcessElement(String id, String name, ElementType type, ProcessElement host, boolean interrupting) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.host = host;
        this.interrupting = interrupting;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ElementType getType() {
        return type;
    }

    public ElementCategory getCategory() {
        return type.getCategory();
    }

    public boolean isBoundaryEvent() {
        return host != null;
    }

    public ProcessElement getHost() {
        return host;
    }

    public boolean isInterrupting() {
        return interrupting;
    }

    public List<ProcessElement> getBoundaryEvents() {
        return Collections.unmodifiableList(boundaryEvents);
    }

    public List<ProcessFlow> getArrivingFlows() {
        return Collections.unmodifiableList(arrivingFlows);
    }

    public List<ProcessFlow> getLeavingFlows() {
        return Collections.unmodifiableList(leavingFlows);
    }

    void addArrivingFlow(ProcessFlow flow) {
        arrivingFlows.add(flow);
    }

    void addLeavingFlow(ProcessFlow flow) {
        leavingFlows.add(flow);
    }

    void addBoundaryEvent(ProcessElement boundaryEvent) {
        boundaryEvents.add(boundaryEvent);
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
