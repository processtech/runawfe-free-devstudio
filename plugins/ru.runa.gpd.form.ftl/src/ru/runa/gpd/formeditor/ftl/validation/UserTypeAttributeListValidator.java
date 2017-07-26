package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.UserTypeVariableListComboParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class UserTypeAttributeListValidator extends DefaultParameterTypeValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        List<ValidationError> list = super.validate(formNode, component, parameter);
        Object value = component.getParameterValue(parameter);
        List<String> attributes = TypeConversionUtil.convertTo(List.class, value);
        VariableUserType userType = getUserType(formNode, component);
        SetView<String> diff = Sets.difference(Sets.newHashSet(attributes), Sets.newHashSet(getAttributes(userType)));
        if (!diff.isEmpty()) {
            list.add(ValidationError.createError(formNode, Messages.getString("validation.componentParameterUserTypeAttribute.unknown", diff,
                    userType.getName(), component.getType().getLabel())));
        }
        return list;
    }

    private List<String> getAttributes(VariableUserType userType) {
        if (userType == null) {
            return Lists.newArrayList();
        }
        return Lists.transform(userType.getAttributes(), new Function<Variable, String>() {

            @Override
            public String apply(Variable variable) {
                return variable.getName();
            }
        });
    }

    private final VariableUserType getUserType(FormNode formNode, Component component) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof UserTypeVariableListComboParameter) {
                String variableName = (String) component.getParameterValue(componentParameter);
                if (variableName != null) {
                    Variable variable = VariableUtils.getVariableByName(formNode.getProcessDefinition(), variableName);
                    return getListVariableUserType(formNode, variable);
                }
            }
        }
        return null;
    }

    private VariableUserType getListVariableUserType(FormNode formNode, Variable variable) {
        if (variable == null) {
            return null;
        }
        String[] componentFormats = variable.getFormatComponentClassNames();
        if (componentFormats.length != 1) {
            return null;
        }
        String userTypeName = componentFormats[0];
        return formNode.getProcessDefinition().getVariableUserType(userTypeName);
    }

}
