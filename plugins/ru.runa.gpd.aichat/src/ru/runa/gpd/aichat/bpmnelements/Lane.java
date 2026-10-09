package ru.runa.gpd.aichat.bpmnelements;

import java.util.List;

public class Lane {
    public String id;
    public String name;
    public List<String> flowNodeRefs;
    public Bounds bounds;
    public Initializer initializer;

    public static class Initializer {
        public String name;
        public String type;
        public String roleSource;

        public Initializer(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public Initializer(String name, String type, String roleSource) {
            this.name = name;
            this.type = type;
            this.roleSource = roleSource;
        }
    }

    public static class Bounds {
        public Integer x;
        public Integer y;
        public Integer width;
        public Integer height;

        public Bounds(Integer x, Integer y, Integer width, Integer height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }
}
