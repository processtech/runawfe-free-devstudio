package ru.runa.gpd.aichat.bpmnelements;


import java.util.Map;

public class ProcessContext {
    public String id;
    public String name;
    public String type;
    public Map<String, Node> elements;
    public Map<String, Connection> connections;
    public Map<String, Lane> lanes;
    public Map<String, Variable> variables;
}