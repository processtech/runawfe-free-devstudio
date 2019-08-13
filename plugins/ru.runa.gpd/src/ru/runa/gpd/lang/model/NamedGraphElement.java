package ru.runa.gpd.lang.model;

import java.util.List;
import org.eclipse.core.resources.IFile;
import ru.runa.gpd.lang.ValidationError;
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
        this.name = name;
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
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        ((NamedGraphElement) copy).setName(getName());
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (getName() == null) {
            errors.add(ValidationError.createLocalizedError(this, "nameNotDefined"));
        }
    }
}
