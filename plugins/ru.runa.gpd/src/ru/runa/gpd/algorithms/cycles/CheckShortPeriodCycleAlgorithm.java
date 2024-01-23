package ru.runa.gpd.algorithms.cycles;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;

public class CheckShortPeriodCycleAlgorithm {

    private List<CycleNode> graph;
    private List<CycleNode> cycle;
    private Node node;

    public CheckShortPeriodCycleAlgorithm(List<Node> nodes, List<Transition> transitions) {
        this.graph = convertToCycleNodes(nodes, transitions);
        graph.sort(null);
        node = null;
    }

    private List<CycleNode> convertToCycleNodes(List<Node> nodes, List<Transition> transitions) {
        Map<Node, CycleNode> mapGraph = new HashMap<>();

        for (int i = 0; i < nodes.size(); ++i) {
            node = nodes.get(i);
            if (node instanceof ParallelGateway) {
                mapGraph.put(node, new ParallelCycleNode(node, i));
            } else {
                mapGraph.put(node, new CycleNode(node, i));
            }
        }
        CycleNode source = null;
        CycleNode target = null;
        CycleNode cycleNode = null;
        for (Transition transition : transitions) {
            source = mapGraph.get(transition.getSource());
            target = mapGraph.get(transition.getTarget());
            if (target instanceof ParallelCycleNode) {
                node = new ExclusiveGateway();
                cycleNode = new CycleNode(node, mapGraph.size());
                mapGraph.put(node, cycleNode);
                source.addTarget(cycleNode);
                cycleNode.addTarget(target);
                ((ParallelCycleNode) target).addParent(cycleNode);
            } else {
                source.addTarget(target);
            }
        }
        return new ArrayList<>(mapGraph.values());
    }

    public void start() {
        GraphOfStates tree = new GraphOfStates(graph);
        tree.createTree();
        cycle = tree.getShortPeriodCycle();
    }

    public boolean hasShortPeriodCycle() {
        return cycle != null;
    }

    public String getCycleIds() {
        if (cycle != null) {
            StringBuilder message = new StringBuilder();
            for (CycleNode node : cycle)
                if (node.getSource().getId() != null) {
                    message.append(node.getSource().getId() + ", ");
                }
            message.replace(message.length() - 2, message.length() - 1, "");
            return message.toString();
        }
        return "";
    }
}