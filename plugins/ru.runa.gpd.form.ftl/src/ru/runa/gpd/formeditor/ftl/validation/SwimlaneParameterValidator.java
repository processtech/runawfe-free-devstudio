package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

import com.google.common.base.Strings;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

public class SwimlaneParameterValidator extends DefaultParameterTypeValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        List<ValidationError> list = super.validate(formNode, component, parameter);
        if (list.isEmpty()) {
            String value = (String) component.getParameterValue(parameter);
            if (parameter.isRequired() && !Strings.isNullOrEmpty(value) && formNode.getProcessDefinition().getSwimlaneByName(value) == null) {
                list.add(ValidationError.createError(formNode,
                        Messages.getString("validation.componentParameterSwimlane.unknown", value, component.getType().getLabel())));
            }
        }
        return list;
    }

}
