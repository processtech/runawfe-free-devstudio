package ru.runa.gpd.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ru.runa.gpd.algorithms.reachability.ElementType;
import ru.runa.gpd.algorithms.reachability.ProcessGraph;
import ru.runa.gpd.lang.model.EmbeddedSubprocess;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.EventSubprocess;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Synchronizable;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.BusinessRule;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.DataStore;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventCapable;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.bpmn.ThrowEventNode;

public class ProcessGraphConverter {
    private final ProcessGraph graph = new ProcessGraph();
    private final List<Node> unsupportedNodes = new ArrayList<>();

    public ProcessGraphConverter(ProcessDefinition definition) {
        boolean repeatedStart = definition instanceof SubprocessDefinition && ((SubprocessDefinition) definition).isTriggeredByEvent();
        for (Node node : definition.getChildren(Node.class)) {
            if (node instanceof DataStore) {
                continue;
            }
            if (node instanceof EventSubprocess) {
                unsupportedNodes.addAll(getBoundaryEvents(node));
                continue;
            }
            // tokens beyond the model: an event subprocess diagram starts again on each trigger,
            // a graph part subprocess returns a token to the parent from each of its ends
            if ((repeatedStart && node instanceof StartState)
                    || (node instanceof Subprocess && canReturnSeveralTokens((Subprocess) node, new HashSet<>()))) {
                unsupportedNodes.add(node);
                continue;
            }
            ElementType type = getElementType(node);
            if (type == null) {
                unsupportedNodes.add(node);
                continue;
            }
            graph.addElement(node.getId(), node.getName(), type);
            for (Node boundaryEvent : getBoundaryEvents(node)) {
                if (isAsync(node)) {
                    unsupportedNodes.add(boundaryEvent);
                } else {
                    graph.addBoundaryEvent(boundaryEvent.getId(), boundaryEvent.getName(), node.getId(), boundaryEvent.isInterruptingBoundaryEvent());
                }
            }
        }
        for (Transition transition : definition.getChildrenRecursive(Transition.class)) {
            String sourceId = transition.getSource().getId();
            String targetId = transition.getTarget().getId();
            if (graph.getElement(sourceId) != null && graph.getElement(targetId) != null) {
                graph.addFlow(transition.getId(), sourceId, targetId);
            }
        }
    }

    public ProcessGraph getGraph() {
        return graph;
    }

    public List<Node> getUnsupportedNodes() {
        return unsupportedNodes;
    }

    private static ElementType getElementType(Node node) {
        if (node instanceof StartState) {
            return ElementType.START;
        }
        if (node instanceof EndState) {
            return ElementType.END;
        }
        if (node instanceof EndTokenState) {
            return ElementType.END_TOKEN;
        }
        if (node instanceof ParallelGateway) {
            return ElementType.PARALLEL_GATEWAY;
        }
        if (node instanceof ExclusiveGateway) {
            return ElementType.EXCLUSIVE_GATEWAY;
        }
        if (node instanceof ScriptTask || node instanceof BusinessRule) {
            return ElementType.AUTOMATIC_TASK;
        }
        if (node instanceof Subprocess) {
            return isAsync(node) ? ElementType.ASYNC_SUBPROCESS : ElementType.SUBPROCESS;
        }
        if (node instanceof TaskState) {
            return isAsync(node) ? ElementType.ASYNC_TASK : ElementType.TASK;
        }
        if (node instanceof Timer || node instanceof CatchEventNode) {
            return ElementType.CATCH_EVENT;
        }
        if (node instanceof ThrowEventNode) {
            return ElementType.THROW_EVENT;
        }
        return null;
    }

    private static boolean isAsync(Node node) {
        return node instanceof Synchronizable && ((Synchronizable) node).isAsync();
    }

    private static boolean canReturnSeveralTokens(Subprocess subprocess, Set<String> visitedIds) {
        SubprocessDefinition definition = subprocess.isEmbedded() ? subprocess.getEmbeddedSubprocess() : null;
        if (definition == null || definition.getBehavior() != EmbeddedSubprocess.Behavior.GraphPart || !visitedIds.add(definition.getId())) {
            return false;
        }
        for (Node node : definition.getChildren(Node.class)) {
            if (node instanceof ParallelGateway && node.getLeavingTransitions().size() > 1) {
                return true;
            }
            if (node instanceof Subprocess && canReturnSeveralTokens((Subprocess) node, visitedIds)) {
                return true;
            }
            for (Node boundaryEvent : getBoundaryEvents(node)) {
                if (!boundaryEvent.isInterruptingBoundaryEvent() && !boundaryEvent.getLeavingTransitions().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<Node> getBoundaryEvents(Node node) {
        List<Node> boundaryEvents = new ArrayList<>();
        for (Node child : node.getChildren(Node.class)) {
            if (child instanceof IBoundaryEventCapable && ((IBoundaryEventCapable) child).isBoundaryEvent()) {
                boundaryEvents.add(child);
            }
        }
        return boundaryEvents;
    }
}
