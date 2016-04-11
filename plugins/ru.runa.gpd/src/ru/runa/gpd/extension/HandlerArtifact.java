package ru.runa.gpd.extension;

import java.util.List;

import com.google.common.collect.Lists;

public class HandlerArtifact extends Artifact {
    public static final String ACTION = "actionHandler";
    public static final String DECISION = "decisionHandler";
    public static final String ASSIGNMENT = "assignmentHandler";
    public static final String TASK_HANDLER = "botTaskHandler";
    private final List<String> types = Lists.newArrayList();
    private String configurerClassName;

    public HandlerArtifact() {
    }

    public HandlerArtifact(boolean enabled, String name, String label, String configurerClassName) {
        super(enabled, name, label);
        setConfigurerClassName(configurerClassName);
    }

    public List<String> getTypes() {
        return types;
    }

    public void addType(String type) {
        this.types.add(type);
    }

    public String getConfigurerClassName() {
        return configurerClassName;
    }

    public void setConfigurerClassName(String configuratorClassName) {
        this.configurerClassName = configuratorClassName;
    }

    @Override
    public String toString() {
        return types + ": " + getName();
    }
}
