package ru.runa.gpd.formeditor.ftl.validation;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

public class DefaultParameterTypeValidator implements IParameterTypeValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        List<ValidationError> list = Lists.newArrayList();
        Object value = component.getParameterValue(parameter);
        if (parameter.isRequired()) {
            if (value instanceof String) {
                if (Strings.isNullOrEmpty((String) value)) {
                    list.add(createRequiredParameterIsNotSetError(formNode, component, parameter));
                }
            } else {
                List<String> strings = (List<String>) value;
                if (strings.size() == 0) {
                    list.add(createRequiredParameterIsNotSetError(formNode, component, parameter));
                }
                for (String string : strings) {
                    if (Strings.isNullOrEmpty(string)) {
                        list.add(createRequiredParameterIsNotSetError(formNode, component, parameter));
                    }
                }
            }
        }
        return list;
    }

    private ValidationError createRequiredParameterIsNotSetError(FormNode formNode, Component component, ComponentParameter parameter) {
        return ValidationError.createError(formNode,
                Messages.getString("validation.requiredComponentParameterIsNotSet", component.getType().getLabel(), component.getId(),
                        parameter.getLabel()),
                new ComponentValidationErrorDetails(formNode, component));
    }

}
