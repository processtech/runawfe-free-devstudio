package ru.runa.gpd.lang.model.bpmn;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import ru.runa.gpd.Localization;

public enum StartEventType {

    blank, timer, message, signal, cancel, conditional, error;

    private final String label = Localization.getString("event.node.type." + name().toLowerCase());

    public String getImageName() {
        return "start/catch_" + name() + ".png";
    }

    public String getNonInterruptingImageName() {
        return "start/catch_" + name() + "_non_interrupting.png";
    }

    public String getLabel() {
        return label;
    }

    public static final String[] LABELS;

    public static final StartEventType[] PROPERTY_TYPES;

    public static final String[] PROPERTY_LABELS;

    static {
        LABELS = Arrays.stream(values())
                .map(StartEventType::getLabel)
                .toArray(String[]::new);

        PROPERTY_TYPES = Arrays.stream(values())
                .filter(type -> type != conditional)
                .toArray(StartEventType[]::new);

        PROPERTY_LABELS = Arrays.stream(PROPERTY_TYPES)
                .map(StartEventType::getLabel)
                .toArray(String[]::new);
    }
}
