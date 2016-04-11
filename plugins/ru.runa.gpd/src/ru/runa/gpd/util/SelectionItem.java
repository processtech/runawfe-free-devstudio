package ru.runa.gpd.util;

public class SelectionItem {
    private boolean enabled;
    private final String label;

    public SelectionItem(boolean enabled, String label) {
        this.enabled = enabled;
        this.label = label;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public String getLabel() {
        return label;
    }
}
