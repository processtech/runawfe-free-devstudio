package ru.runa.gpd.aichat.utils;

import ru.runa.gpd.aichat.bpmnelements.Connection;
import ru.runa.gpd.aichat.bpmnelements.GraphElement;
import ru.runa.gpd.aichat.bpmnelements.Node;
import ru.runa.gpd.aichat.bpmnelements.ProcessContext;

import java.util.*;

public class AutoHorizontalLayout {
//    private static final int SPACING_HORIZONTAL = 200;
//    private static final int SPACING_VERTICAL = 130;
    private static final int SPACING_HORIZONTAL = 250;
    private static final int SPACING_VERTICAL = 130;
    private static final int SPACING_START_X = 100;
    private static final int SPACING_START_Y = 200;

    public static void applyLayout(final ProcessContext process) {

        Map<String, GraphElement> graph = buildGraph(process);

        Map<Integer, List<GraphElement>> levels = new TreeMap<>();

        List<GraphElement> startNodes = new ArrayList<>();
        for (GraphElement n : graph.values()) {
            if (n.incoming.isEmpty()) {
                startNodes.add(n);
            }
        }

        Set<String> visited = new HashSet<>();
        for (GraphElement start : startNodes) {
            assignLevels(start, 0, graph, levels, visited);
        }

        for (Map.Entry<Integer, List<GraphElement>> entry : levels.entrySet()) {

            int level = entry.getKey();
            List<GraphElement> nodes = entry.getValue();

            int x = SPACING_START_X + level * SPACING_HORIZONTAL;
            int y = SPACING_START_Y;

            for (GraphElement node : nodes) {
                node.node.position = new Node.Position(x, y);
                y += SPACING_VERTICAL;
            }
        }

        for (GraphElement ge : graph.values()) {
            Node n = process.elements.get(ge.node.id);
            if (n != null && ge.node.position != null) {
                n.position = ge.node.position;
            }
        }
    }

    private static Map<String, GraphElement> buildGraph(ProcessContext process) {

        Map<String, GraphElement> graph = new HashMap<>();

        for (Node node : process.elements.values()) {
            GraphElement ge = new GraphElement();
            ge.node = node;
            ge.incoming = new ArrayList<>();
            ge.outgoing = new ArrayList<>();
            graph.put(node.id, ge);
        }

        for (Connection c : process.connections.values()) {
            GraphElement source = graph.get(c.source);
            GraphElement target = graph.get(c.target);

            if (source != null && target != null) {
                source.outgoing.add(target.node.id);
                target.incoming.add(source.node.id);
            }
        }

        return graph;
    }

    private static void assignLevels(
            GraphElement node,
            int level,
            Map<String, GraphElement> graph,
            Map<Integer, List<GraphElement>> levels,
            Set<String> visited
    ) {
        if (!visited.add(node.node.id)) {
            return;
        }

        node.level = level;
        levels.computeIfAbsent(level, k -> new ArrayList<>()).add(node);

        for (String nextId : node.outgoing) {
            GraphElement next = graph.get(nextId);
            if (next != null) {
                assignLevels(next, level + 1, graph, levels, visited);
            }
        }
    }
}