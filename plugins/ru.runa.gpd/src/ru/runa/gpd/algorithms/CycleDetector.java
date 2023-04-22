package ru.runa.gpd.algorithms;

import java.util.Queue;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;


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
            if (currentNode instanceof Decision || currentNode instanceof ScriptTask) {
                boolean isCycle = traverseNode(currentNode, new HashSet<>());
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

    private boolean traverseNode(Node node, Set<Node> visited) {
        visited.add(node);
        for (AbstractTransition outgoingNode : node.getLeavingTransitions()) {
            if (outgoingNode.getTarget() instanceof Decision
                || outgoingNode.getTarget() instanceof ScriptTask) {
                Node nextNode = outgoingNode.getTarget();
                if (visited.contains(nextNode)) {
                    return true;
                }
                boolean isCycle = traverseNode(nextNode, visited);
                if (isCycle) {
                    return true;
                }
            }
        }
        visited.remove(node);
        return false;
    }
}