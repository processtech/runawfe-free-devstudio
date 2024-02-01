package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

public enum TaskStateExecutionButton {
    NONE,
    BY_SERVER_CONSTANT,
    BY_LEAVING_TRANSITION_NAME;

    public String getLabel() {
        return Localization.getString("TaskStateExecutionButton." + this.name());
    }

    public static String[] getLabels() {
        TaskStateExecutionButton[] values = TaskStateExecutionButton.values();
        int valuesLength = values.length;
        String[] labels = new String[valuesLength];
        for (int i = 0; i < valuesLength; i++) {
            labels[i] = values[i].getLabel();
        }
        return labels;
    }

    public String stringValueForSaving() {
        switch (this) {
        case BY_LEAVING_TRANSITION_NAME:
            return "true";
        case BY_SERVER_CONSTANT:
            return "false";
        case NONE:
            return null;
        default:
            return null;
        }
    }

    public static TaskStateExecutionButton parseFromSaving(String saving) {
        return Boolean.parseBoolean(saving) ? BY_LEAVING_TRANSITION_NAME : BY_SERVER_CONSTANT;
    }
}
