package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

public enum EndTokenSubprocessDefinitionBehavior {
    BACK_TO_BASE_PROCESS,
    TERMINATE;

    public String getLabel() {
        return Localization.getString("EndTokenSubprocessDefinitionBehavior." + name());
    }

    public static final String[] getLabels() {
        String[] labels = new String[EndTokenSubprocessDefinitionBehavior.values().length];
        int i = 0;
        for (EndTokenSubprocessDefinitionBehavior e : EndTokenSubprocessDefinitionBehavior.values()) {
            labels[i] = e.getLabel();
            i++;
        }
        return labels;
    }
}
