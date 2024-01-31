package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;
import java.util.stream.Collectors;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;

public class DbUserTypeListComboParameter extends ComboParameter {
    public DbUserTypeListComboParameter() {
        super(false, true);
    }

    @Override
    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        return processDefinition.getVariableUserTypes().stream().filter(VariableUserType::isStoreInExternalStorage)
                .map(userType -> new ComboOption(userType.getName(), userType.getName())).collect(Collectors.toList());
    }

}
