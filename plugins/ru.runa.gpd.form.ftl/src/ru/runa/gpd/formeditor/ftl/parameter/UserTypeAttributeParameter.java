package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

public class UserTypeAttributeParameter extends ComboParameter {

    public UserTypeAttributeParameter() {
        super();
    }

    public UserTypeAttributeParameter(boolean multiple, boolean autoSelectSingleOption) {
        super(multiple, autoSelectSingleOption);
    }

    public UserTypeAttributeParameter(boolean multiple) {
        super(multiple);
    }

    @Override
    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        VariableUserType userType = getUserType(component, processDefinition);
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

    protected VariableUserType getUserType(Component component, ProcessDefinition processDefinition) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof UserTypeVariableListComboParameter
                    || componentParameter.getType() instanceof VariableComboParameter) {
                String variableName = (String) component.getParameterValue(componentParameter);
                if (!Strings.isNullOrEmpty(variableName)) {
                    Variable variable = getVariables(componentParameter, processDefinition).get(variableName);
                    return getVariableUserType(variable, processDefinition);
                }
            }
        }
        return null;
    }
}
