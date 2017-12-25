package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

import com.google.common.base.Objects;

public enum ProcessRegulationSwimlane {
    YES, NO;

    private final String value;
    public static final String[] LABELS = new String[ProcessRegulationSwimlane.values().length];
    static {
        for (int i = 0; i < ProcessRegulationSwimlane.values().length; i++) {
            LABELS[i] = ProcessRegulationSwimlane.values()[i].getLabel();
        }
    }

    private ProcessRegulationSwimlane() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return Localization.getString("ProcessRegulationSwimlane." + value);
    }

    public static ProcessRegulationSwimlane getByValueNotNull(String value) {
        for (ProcessRegulationSwimlane processRegulationSwimlane : ProcessRegulationSwimlane.values()) {
            if (Objects.equal(processRegulationSwimlane.getValue(), value)) {
                return processRegulationSwimlane;
            }
        }
        throw new RuntimeException("No ProcessRegulationSwimlane found by value = '" + value + "'");
    }

}
