package ru.runa.gpd.algorithms.cycles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;

import static java.util.stream.Collectors.joining;

public class CheckShortPeriodCycleAlgorithm {
    private List<CycleNode> graph;
    private List<CycleNode> cycle;
    private Node node;

    public CheckShortPeriodCycleAlgorithm(List<Node> nodes, List<Transition> transitions) {
        this.graph = convertToCycleNodes(nodes, transitions);
        graph.sort(null);
        node = null;
        GraphOfStates tree = new GraphOfStates(graph);
        cycle = tree.getShortPeriodCycle();
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
        for (Transition transition : transitions) {
            CycleNode source = mapGraph.get(transition.getSource());
            CycleNode target = mapGraph.get(transition.getTarget());
            if (target instanceof ParallelCycleNode) {
                String pseudoId = source.getSource().getId() + " -> " + target.getSource().getId();
                node = new ExclusiveGateway();
                node.setId(pseudoId);
                node.setName("Pseudo Gateway " + mapGraph.size());
                CycleNode pseudoNode = new CycleNode(node, mapGraph.size());
                mapGraph.put(node, pseudoNode);
                source.addTarget(pseudoNode);
                pseudoNode.addTarget(target);
                ((ParallelCycleNode) target).addParent(pseudoNode);
            } else {
                source.addTarget(target);
            }
        }
        return new ArrayList<>(mapGraph.values());
    }

    public boolean hasShortPeriodCycle() {
        return cycle != null;
    }

    public String getCycleIds() {
        return cycle.stream()
                .filter(n -> n.getSource().getId() != null && !n.getSource().getId().contains(" -> "))
                .map(n -> n.getSource().toString())
                .collect(joining(" -> "));
    }
}