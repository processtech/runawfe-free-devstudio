package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

import com.google.common.base.Objects;

public enum ProcessRegulations {
    DEFAULT, USER_ORDER;

    private final String value;
    public static final String[] LABELS = new String[ProcessRegulations.values().length];
    static {
        for (int i = 0; i < ProcessRegulations.values().length; i++) {
            LABELS[i] = ProcessRegulations.values()[i].getLabel();
        }
    }

    private ProcessRegulations() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return Localization.getString("ProcessRegulations." + value);
    }

    public static ProcessRegulations getByValueNotNull(String value) {
        for (ProcessRegulations processRegulations : ProcessRegulations.values()) {
            if (Objects.equal(processRegulations.getValue(), value)) {
                return processRegulations;
            }
        }
        throw new RuntimeException("No ProcessRegulations found by value = '" + value + "'");
    }

}
