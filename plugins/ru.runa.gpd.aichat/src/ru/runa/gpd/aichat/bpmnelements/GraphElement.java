package ru.runa.gpd.aichat.bpmnelements;

import java.util.List;

public class GraphElement {
    public Node node;
    public List<String> incoming;
    public List<String> outgoing;
    public int level;
}
