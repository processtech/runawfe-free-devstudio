package ru.runa.gpd.algorithms;

import java.util.Queue;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;


public class CycleDetector {

    private Set<Node> visited;
    private Queue<Node> queue;

    public CycleDetector() {
        visited = new HashSet<>();
        queue = new PriorityQueue<>();
    }


    public boolean hasCycle(Node startNode) {
        visited.add(startNode);
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            if (currentNode instanceof Decision || currentNode instanceof ScriptTask
                || currentNode instanceof ParallelGateway) {
                boolean isCycle = traverseNode(currentNode);
                if (isCycle) {
                    return true;
                }
            }
            for (AbstractTransition outgoingNode : currentNode.getLeavingTransitions()) {
                if (!visited.contains(outgoingNode.getTarget())) {
                    visited.add(outgoingNode.getTarget());
                    queue.add(outgoingNode.getTarget());
                }
            }
        }
        return false;
    }

    private boolean traverseNode(Node startNode) {
        Queue<Node> queue = new PriorityQueue<>();
        Set<Node> visited = new HashSet<>();
        queue.add(startNode);
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            if (visited.contains(node)) {
                return true;
            }
            visited.add(node);
            for (AbstractTransition outgoingNode : node.getLeavingTransitions()) {
                if (outgoingNode.getTarget() instanceof Decision
                    || outgoingNode.getTarget() instanceof ScriptTask
                    || outgoingNode.getTarget() instanceof ParallelGateway) {
                    queue.add(outgoingNode.getTarget());
                } else {
                    if (node instanceof ParallelGateway) {
                        return false;
                    }
                }
            }
        }
        return false;
    }
}