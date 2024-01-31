package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.collect.Lists;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.util.VariablesCache;
import ru.runa.gpd.formeditor.ftl.validation.IParameterTypeValidator;
import ru.runa.gpd.lang.model.ProcessDefinition;
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

    public Object fromPropertyDescriptorValue(Component component, ComponentParameter parameter, Object editorValue,
            ProcessDefinition processDefinition) {
        return editorValue;
    }

    public Object toPropertyDescriptorValue(Component component, ComponentParameter parameter, Object value, ProcessDefinition processDefinition) {
        return value;
    }

    public abstract PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId,
            ProcessDefinition processDefinition);

    public abstract Object createEditor(Composite parent, Component component, ComponentParameter parameter, Object oldValue,
            PropertyChangeListener listener, ProcessDefinition processDefinition);

    public void updateEditor(Object ui, Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {

    }

    public List<String> getVariableNames(ComponentParameter parameter, ProcessDefinition processDefinition) {
        Map<String, Variable> variablesMap = VariablesCache.getInstance().getVariables(parameter.getVariableTypeFilter(), processDefinition);
        List<String> list = Lists.newArrayList(variablesMap.keySet());
        Collections.sort(list);
        return list;
    }

    protected Map<String, Variable> getVariables(ComponentParameter parameter, ProcessDefinition processDefinition) {
        return VariablesCache.getInstance().getVariables(parameter.getVariableTypeFilter(), processDefinition);
    }

    protected VariableUserType getVariableUserType(Variable variable, ProcessDefinition processDefinition) {
        if (variable == null) {
            return null;
        }
        String[] componentFormats = variable.getFormatComponentClassNames();
        if (componentFormats.length == 0) {
            return variable.getUserType();
        }
        String userTypeName = componentFormats[0];
        return processDefinition.getVariableUserType(userTypeName);
    }
}
