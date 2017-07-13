package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

import com.google.common.collect.Lists;

public class UserTypeVariableListComboParameter extends ComboParameter {

    @Override
    protected List<ComboOption> getOptions(Component component, ComponentParameter parameter) {
        List<ComboOption> result = Lists.newArrayList();
        for (Variable variable : getVariables(parameter).values()) {
            VariableUserType userType = getListVariableUserType(variable);
            if (userType == null) {
                continue;
            }
            result.add(new ComboOption(variable.getName(), variable.getName()));
        }
        return result;
    }

}
