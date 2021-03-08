package ru.runa.gpd.lang.model.bpmn;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.Localization;

public enum EventNodeType {
    message,
    signal,
    cancel,
    error;

    public String getImageName(boolean isCatch, boolean boundary) {
        return (boundary ? "boundary_" : "") + (isCatch ? "catch" : "throw") + "_" + name() + ".png";
    }

    private String label = Localization.getString("event.node.type." + name().toLowerCase());

    public static String[] LABELS;

    static {
        List<String> eventTypeLabels = Lists.newArrayList();
        for (EventNodeType eventType : EventNodeType.values()) {
            eventTypeLabels.add(eventType.label);
        }
        LABELS = eventTypeLabels.toArray(new String[eventTypeLabels.size()]);
    }
}
