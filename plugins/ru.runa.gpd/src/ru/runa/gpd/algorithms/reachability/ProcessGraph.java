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
        return addElement(new ProcessElement(id, name, type, null, false));
    }

    public ProcessElement addBoundaryEvent(String id, String name, String hostId, boolean interrupting) {
        ProcessElement host = getExistingElement(hostId);
        if (host.getType().isInstant() || host.isBoundaryEvent()) {
            throw new IllegalArgumentException("Element " + hostId + " cannot have boundary events");
        }
        ProcessElement boundaryEvent = addElement(new ProcessElement(id, name, ElementType.CATCH_EVENT, host, interrupting));
        host.addBoundaryEvent(boundaryEvent);
        return boundaryEvent;
    }

    public ProcessFlow addFlow(String id, String sourceId, String targetId) {
        ProcessElement source = getExistingElement(sourceId);
        ProcessElement target = getExistingElement(targetId);
        if (target.isBoundaryEvent()) {
            throw new IllegalArgumentException("Boundary event " + targetId + " cannot have arriving flows");
        }
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

    private ProcessElement addElement(ProcessElement element) {
        if (elements.containsKey(element.getId())) {
            throw new IllegalArgumentException("Duplicate element id: " + element.getId());
        }
        elements.put(element.getId(), element);
        return element;
    }

    private ProcessElement getExistingElement(String id) {
        ProcessElement element = elements.get(id);
        if (element == null) {
            throw new IllegalArgumentException("Unknown element id: " + id);
        }
        return element;
    }
}
