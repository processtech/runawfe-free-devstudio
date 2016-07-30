package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

import com.google.common.base.Objects;

public enum NodeAsyncExecution {
    DEFAULT, SHARED, NEW;

    private final String value;
    public static final String[] LABELS = new String[NodeAsyncExecution.values().length];
    static {
        for (int i = 0; i < NodeAsyncExecution.values().length; i++) {
            LABELS[i] = NodeAsyncExecution.values()[i].getLabel();
        }
    }

    private NodeAsyncExecution() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return Localization.getString("NodeAsyncExecution." + value);
    }

    public static NodeAsyncExecution getByValueNotNull(String value) {
        for (NodeAsyncExecution nodeExecutionTransaction : NodeAsyncExecution.values()) {
            if (Objects.equal(nodeExecutionTransaction.getValue(), value)) {
                return nodeExecutionTransaction;
            }
        }
        throw new RuntimeException("No NodeAsyncExecution found by value = '" + value + "'");
    }

}
