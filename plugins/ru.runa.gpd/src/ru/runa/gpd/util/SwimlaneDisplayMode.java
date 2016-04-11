package ru.runa.gpd.util;

import ru.runa.gpd.Localization;

public enum SwimlaneDisplayMode {
    none("SwimlaneDisplayMode.none"), horizontal("SwimlaneDisplayMode.horizontal"), vertical("SwimlaneDisplayMode.vertical");
    private String label;

    private SwimlaneDisplayMode(String labelKey) {
        this.label = Localization.getString(labelKey);
    }

    public String getLabel() {
        return label;
    }
}
