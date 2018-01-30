package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.validation.IParameterTypeValidator;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

public abstract class ParameterType {
    private final boolean multiple;
    private String depends;
    private IParameterTypeValidator validator;

    public ParameterType(boolean multiple) {
        this.multiple = multiple;
    }

    public ParameterType() {
        this(false);
    }

    public boolean isMultiple() {
        return multiple;
    }

    public String getDepends() {
        return depends;
    }

    public void setDepends(String depends) {
        this.depends = depends;
    }

    public IParameterTypeValidator getValidator() {
        return validator;
    }

    public void setValidator(IParameterTypeValidator validator) {
        this.validator = validator;
    }

    public Object fromPropertyDescriptorValue(Component component, ComponentParameter parameter, Object editorValue) {
        return editorValue;
    }

    public Object toPropertyDescriptorValue(Component component, ComponentParameter parameter, Object value) {
        return value;
    }

    public abstract PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId);

    public abstract Object createEditor(Composite parent, Component component, ComponentParameter parameter, Object oldValue,
            PropertyChangeListener listener);

    public void updateEditor(Object ui, Component component, ComponentParameter parameter) {

    }

    protected List<String> getVariableNames(ComponentParameter parameter) {
        return FormEditor.getCurrent().getVariableNames(parameter.getVariableTypeFilter());
    }

    protected Map<String, Variable> getVariables(ComponentParameter parameter) {
        return FormEditor.getCurrent().getVariables(parameter.getVariableTypeFilter());
    }

    protected VariableUserType getListVariableUserType(Variable variable) {
        if (variable == null) {
            return null;
        }
        String[] componentFormats = variable.getFormatComponentClassNames();
        if (componentFormats.length != 1) {
            return null;
        }
        String userTypeName = componentFormats[0];
        return FormEditor.getCurrent().getVariableUserType(userTypeName);
    }
}
