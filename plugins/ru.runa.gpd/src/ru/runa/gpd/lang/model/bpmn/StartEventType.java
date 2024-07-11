package ru.runa.gpd.lang.model.bpmn;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.Localization;

public enum StartEventType {

    blank, timer, message, signal, cancel, error;

    private String label = Localization.getString("event.node.type." + name().toLowerCase());

    public String getImageName() {
        return "start/catch_" + name() + ".png";
    }

    public String getNonInterruptingImageName() {
        return "start/catch_" + name() + "_non_interrupting.png";
    }

    public String getLabel() {
        return label;
    }

    public static String[] LABELS;

    static {
        List<String> eventTypeLabels = Lists.newArrayList();
        for (StartEventType eventType : StartEventType.values()) {
            eventTypeLabels.add(eventType.label);
        }
        LABELS = eventTypeLabels.toArray(new String[eventTypeLabels.size()]);
    }
}
