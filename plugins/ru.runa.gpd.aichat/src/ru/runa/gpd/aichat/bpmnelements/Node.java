package ru.runa.gpd.aichat.bpmnelements;

import java.util.List;
import java.util.Map;

public class Node {
    public String id;
    public String type;
    public String name;
    public Position position;
    public Sizing sizing;
    public Map<String, Object> properties;
    public String laneId;
    public String handlerClass;
    public String config;
    public String variableFileName;
    public String template;
    public String executor;
    public String format;
    public String result;
    public List<FormItem<String, String>> form;

    public static class Position {
        public Integer x;
        public Integer y;

        public Position(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Sizing {
        public Integer width;
        public Integer height;

        public Sizing(Integer width, Integer height) {
            this.width = width;
            this.height = height;
        }
    }
}