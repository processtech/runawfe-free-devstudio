package ru.runa.gpd.lang.model;

import org.eclipse.jface.dialogs.IInputValidator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.change.ChangeNameFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
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

    public IInputValidator nameValidator() {
        return (String name) -> {
            if (name != null) {
                name = name.trim();
            } else {
                return Localization.getString("VariableNamePage.error.empty", name);
            }
            if (name.equals(getName())) {
                return Localization.getString("RenameAction.error.new.name.equals.old", name);
            }
            return null;
        };
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
            UndoRedoUtil.executeFeature(new ChangeNameFeature(this, (String) value));
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

}
