package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

import com.google.common.base.Objects;

public enum ProcessRegulationVariable {
    YES, NO;

    private final String value;
    public static final String[] LABELS = new String[ProcessRegulationVariable.values().length];
    static {
        for (int i = 0; i < ProcessRegulationVariable.values().length; i++) {
            LABELS[i] = ProcessRegulationVariable.values()[i].getLabel();
        }
    }

    private ProcessRegulationVariable() {
        this.value = name().toLowerCase();
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return Localization.getString("ProcessRegulationVariable." + value);
    }

    public static ProcessRegulationVariable getByValueNotNull(String value) {
        for (ProcessRegulationVariable processRegulationVariable : ProcessRegulationVariable.values()) {
            if (Objects.equal(processRegulationVariable.getValue(), value)) {
                return processRegulationVariable;
            }
        }
        throw new RuntimeException("No ProcessRegulationVariable found by value = '" + value + "'");
    }

}
