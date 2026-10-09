package ru.runa.gpd.lang.model.bpmn;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.Localization;

public enum EndEventType {

    blank,
    message,
    signal,
    cancel,
    error;

    private String label = Localization.getString("event.type." + name().toLowerCase());

    public String getImageName() {
        return "end/throw_" + name() + ".png";
    }

    public static String[] LABELS;

    static {
        List<String> eventTypeLabels = Lists.newArrayList();
        for (EndEventType eventType : EndEventType.values()) {
            eventTypeLabels.add(eventType.label);
        }
        LABELS = eventTypeLabels.toArray(new String[eventTypeLabels.size()]);
    }
}
