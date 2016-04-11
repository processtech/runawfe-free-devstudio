package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeListener;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.ComponentParameter;

public abstract class ParameterType {
    private final boolean multiple;
    private final boolean surroundBrackets;

    public ParameterType(boolean multiple, boolean surroundBrackets) {
        this.multiple = multiple;
        this.surroundBrackets = surroundBrackets;
    }

    public ParameterType() {
        this(false, true);
    }

    public boolean isMultiple() {
        return multiple;
    }

    public boolean isSurroundBrackets() {
        return surroundBrackets;
    }

    public Object fromPropertyDescriptorValue(ComponentParameter parameter, Object editorValue) {
        return editorValue;
    }

    public Object toPropertyDescriptorValue(ComponentParameter parameter, Object value) {
        return value;
    }

    public abstract PropertyDescriptor createPropertyDescriptor(ComponentParameter parameter, int propertyId);

    public abstract Composite createEditor(Composite parent, ComponentParameter parameter, Object oldValue, PropertyChangeListener listener);

}
