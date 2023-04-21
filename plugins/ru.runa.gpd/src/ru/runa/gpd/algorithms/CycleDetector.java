package ru.runa.gpd.algorithms;

import java.util.Queue;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Decision;


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
            if (currentNode instanceof Decision) {
                boolean isCycle = traverseDecision((Decision) currentNode, new HashSet<>());
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

    private boolean traverseDecision(Decision decision, Set<Decision> visited) {
        visited.add(decision);
        for (AbstractTransition outgoingNode : decision.getLeavingTransitions()) {
            if (outgoingNode.getTarget() instanceof Decision) {
                Decision nextDecision = (Decision) outgoingNode.getTarget();
                if (visited.contains(nextDecision)) {
                    return true;
                }
                boolean isCycle = traverseDecision(nextDecision, visited);
                if (isCycle) {
                    return true;
                }
            }
        }
        visited.remove(decision);
        return false;
    }
}