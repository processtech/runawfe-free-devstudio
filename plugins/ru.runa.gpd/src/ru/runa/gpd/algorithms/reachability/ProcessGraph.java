package ru.runa.gpd.algorithms.reachability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessGraph {
    private final Map<String, ProcessElement> elements = new LinkedHashMap<>();
    private final List<ProcessFlow> flows = new ArrayList<>();

    public ProcessElement addElement(String id, String name, ElementType type) {
        if (elements.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate element id: " + id);
        }
        ProcessElement element = new ProcessElement(id, name, type);
        elements.put(id, element);
        return element;
    }

    public ProcessFlow addFlow(String id, String sourceId, String targetId) {
        ProcessElement source = getExistingElement(sourceId);
        ProcessElement target = getExistingElement(targetId);
        ProcessFlow flow = new ProcessFlow(id, source, target);
        source.addLeavingFlow(flow);
        target.addArrivingFlow(flow);
        flows.add(flow);
        return flow;
    }

    public ProcessElement getElement(String id) {
        return elements.get(id);
    }

    public List<ProcessElement> getElements() {
        return Collections.unmodifiableList(new ArrayList<>(elements.values()));
    }

    public List<ProcessFlow> getFlows() {
        return Collections.unmodifiableList(flows);
    }

    private ProcessElement getExistingElement(String id) {
        ProcessElement element = elements.get(id);
        if (element == null) {
            throw new IllegalArgumentException("Unknown element id: " + id);
        }
        return element;
    }
}
