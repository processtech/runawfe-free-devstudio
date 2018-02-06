package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;

public class VariableParameterTypeValidator extends DefaultParameterTypeValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        List<ValidationError> list = super.validate(formNode, component, parameter);
        if (list.isEmpty()) {
            Object value = component.getParameterValue(parameter);
            if (value instanceof String) {
                validateVariable(formNode, component, parameter, list, (String) value);
            } else {
                for (String variableName : (List<String>) value) {
                    validateVariable(formNode, component, parameter, list, variableName);
                }
            }
        }
        return list;
    }

    private void validateVariable(FormNode formNode, Component component, ComponentParameter parameter, List<ValidationError> list,
            String variableName) {
        if (!parameter.isRequired() && Strings.isNullOrEmpty(variableName)) {
            return;
        }
        Variable variable = VariableUtils.getVariableByName(formNode, variableName);
        if (variable == null) {
            list.add(ValidationError.createError(formNode,
                    Messages.getString("validation.variable.unknown", variableName, component.getType().getLabel())));
        } else if (!Strings.isNullOrEmpty(parameter.getVariableTypeFilter())
                && !VariableFormatRegistry.isApplicable(variable, parameter.getVariableTypeFilter())) {
            if (VariableFormatRegistry.isAssignableFrom(variable.getJavaClassName(), parameter.getVariableTypeFilter())) {
                list.add(ValidationError.createWarning(formNode,
                        Messages.getString("validation.variable.invalid.type", variableName, component.getType().getLabel())));
            } else {
                list.add(ValidationError.createError(formNode,
                        Messages.getString("validation.variable.invalid.type", variableName, component.getType().getLabel())));
            }
        }
    }
}
