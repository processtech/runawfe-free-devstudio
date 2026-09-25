package ru.runa.gpd.algorithms.reachability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProcessElement {
    private final String id;
    private final String name;
    private final ElementType type;
    private final List<ProcessFlow> arrivingFlows = new ArrayList<>();
    private final List<ProcessFlow> leavingFlows = new ArrayList<>();

    ProcessElement(String id, String name, ElementType type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
