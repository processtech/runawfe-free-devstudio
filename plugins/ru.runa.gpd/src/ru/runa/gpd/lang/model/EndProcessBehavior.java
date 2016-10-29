package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

public enum EndProcessBehavior {
    BACK_TO_BASE_PROCESS, TERMINATE;
    
    public String getLabel() {
        return Localization.getString("EndProcessBehavior." + name());
    }

    public static final String[] getLabels() {
        String[] labels = new String[EndProcessBehavior.values().length];
        int i = 0;
        for (EndProcessBehavior e: EndProcessBehavior.values()) {
            labels[i] = e.getLabel();
            i++;
        }
        
        return labels;
    }
}
