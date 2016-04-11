package ru.runa.gpd.lang.model;


public class Event extends GraphElement {
    public static final String NODE_ENTER = "node-enter";
    public static final String NODE_ACTION = "on-node";
    public static final String NODE_LEAVE= "node-leave";
    
    public static final String TASK_CREATE = "task-create";
    public static final String TASK_START = "task-start";
    public static final String TASK_ASSIGN = "task-assign";
    public static final String TASK_END = "task-end";
    
    public static final String TRANSITION = "transition";

    private String type;
    
    public Event(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
}
