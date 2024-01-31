package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

public class UserTypeVariableListComboParameter extends ComboParameter {

    @Override
    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        List<ComboOption> result = Lists.newArrayList();
        for (Variable variable : getVariables(parameter, processDefinition).values()) {
            VariableUserType userType = getVariableUserType(variable, processDefinition);
            if (userType == null) {
                continue;
            }
            result.add(new ComboOption(variable.getName(), variable.getName()));
        }
        return result;
    }
}
