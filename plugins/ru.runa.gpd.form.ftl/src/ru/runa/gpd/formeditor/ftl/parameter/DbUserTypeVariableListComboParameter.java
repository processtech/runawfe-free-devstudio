package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.wfe.var.format.ListFormat;

public class DbUserTypeVariableListComboParameter extends UserTypeAttributeParameter {

    public DbUserTypeVariableListComboParameter() {
        super(false, true);
    }

    @Override
    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        final VariableUserType userType = getUserType(component, processDefinition);
        if (userType == null) {
            return Lists.newArrayList();
        }

        Collection<Variable> values = getVariables(parameter, processDefinition).values();
        if (isSelectLists(component)) {
            return values.stream()
                    .filter(variable -> variable.getFormatClassName().equals(ListFormat.class.getName())
                            && variable.getFormatComponentClassNames()[0].equals(userType.getName()))
                    .map(variable -> new ComboOption(variable.getName(), variable.getName())).collect(Collectors.toList());
        } else {
            return values.stream().filter(Variable::isComplex).filter(variable -> variable.getUserType().equals(userType))
                    .map(variable -> new ComboOption(variable.getName(), variable.getName())).collect(Collectors.toList());
        }
    }

    @Override
    protected VariableUserType getUserType(Component component, ProcessDefinition processDefinition) {
        final Optional<ComponentParameter> dbUserTypeParameter = component.getType().getParameters().stream()
                .filter(parameter -> parameter.getType() instanceof DbUserTypeListComboParameter).findFirst();
        return dbUserTypeParameter.map(parameter -> processDefinition.getVariableUserType((String) component.getParameterValue(parameter)))
                .orElse(null);
    }

    private final boolean isSelectLists(Component component) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof ComboParameter) {
                final String multiplicity = (String) component.getParameterValue(componentParameter);
                if (multiplicity.equals("one")) {
                    return false;
                }
            }
        }
        return true;
    }
}
