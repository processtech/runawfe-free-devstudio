package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

public class DbUserTypeListComboParameter extends ComboParameter {
    @Override
    protected List<ComboOption> getOptions(Component component, ComponentParameter parameter) {
        final Set<ComboOption> result = Sets.newHashSet();
        for (Variable variable : getVariables(parameter).values()) {
            final VariableUserType userType = getVariableUserType(variable);
            if (userType == null || !userType.isStoreInExternalStorage()) {
                continue;
            }
            result.add(new ComboOption(userType.getName(), userType.getName()));
        }
        return new ArrayList<>(result);
    }

}
