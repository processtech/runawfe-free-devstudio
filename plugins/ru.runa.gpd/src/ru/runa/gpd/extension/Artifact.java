package ru.runa.gpd.extension;

import com.google.common.base.Objects;

public class Artifact implements Comparable<Artifact> {
    private boolean enabled;
    private String name;
    private String label;

    public Artifact() {
    }

    public Artifact(Artifact artifact) {
        this.enabled = artifact.isEnabled();
        this.name = artifact.getName();
        this.label = artifact.getLabel();
    }

    public Artifact(boolean enabled, String name, String label) {
        this.enabled = enabled;
        this.name = name;
        this.label = label;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    @Override
    public int compareTo(Artifact o) {
        if (label == null || o.label == null) {
            return -1;
        }
        return label.compareTo(o.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getClass(), name);
    }

    @Override
    public boolean equals(Object obj) {
        return null != obj && Objects.equal(getClass(), obj.getClass())
                && Objects.equal(name, ((Artifact) obj).name);
    }
}
