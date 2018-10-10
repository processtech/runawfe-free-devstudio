package ru.runa.gpd.lang.model;

import com.google.common.base.Strings;
import ru.runa.gpd.settings.PrefConstants;

public abstract class NamedGraphElement extends GraphElement implements Comparable<NamedGraphElement>, PrefConstants {
    private String name;

    public NamedGraphElement() {
    }

    protected NamedGraphElement(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String old = this.getName();
        this.name = adjustName(name);
        firePropertyChange(PROPERTY_NAME, old, this.getName());
    }

    protected boolean canNameBeSetFromProperties() {
        return true;
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_NAME.equals(id)) {
            return safeStringValue(name);
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_NAME.equals(id)) {
            setName((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public String toString() {
        return name + " (" + getId() + ")";
    }

    @Override
    public String getLabel() {
        return name + (getId() != null ? " (" + getId() + ")" : "");
    }

    @Override
    public int compareTo(NamedGraphElement o) {
        if (name == null) {
            return -1;
        }
        if (o == null || o.name == null) {
            return 1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public NamedGraphElement makeCopy(GraphElement parent) {
        NamedGraphElement copy = (NamedGraphElement) super.makeCopy(parent);
        copy.setName(getName());
        return copy;
    }

    private String adjustName(String name) {
        if (!Strings.isNullOrEmpty(name)) {
            name = name.trim().replaceAll("\\s{2,}", " ");
        }
        return name;
    }

}
