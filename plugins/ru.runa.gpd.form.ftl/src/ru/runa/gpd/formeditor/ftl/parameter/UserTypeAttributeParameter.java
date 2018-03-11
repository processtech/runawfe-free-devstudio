package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class UserTypeAttributeParameter extends ComboParameter {

    @Override
    protected List<ComboOption> getOptions(Component component, ComponentParameter parameter) {
        VariableUserType userType = getUserType(component);
        if (userType == null) {
            return Lists.newArrayList();
        }
        return Lists.transform(userType.getAttributes(), new Function<Variable, ComboOption>() {

            @Override
            public ComboOption apply(Variable variable) {
                return new ComboOption(variable.getName(), variable.getName());
            }
        });
    }

    private final VariableUserType getUserType(Component component) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof UserTypeVariableListComboParameter
                    || componentParameter.getType() instanceof VariableComboParameter) {
                String variableName = (String) component.getParameterValue(componentParameter);
                if (!Strings.isNullOrEmpty(variableName)) {
                    Variable variable = getVariables(componentParameter).get(variableName);
                    return getVariableUserType(variable);
                }
            }
        }
        return null;
    }
}
