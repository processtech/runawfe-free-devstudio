package ru.runa.gpd.formeditor.ftl;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import ru.runa.gpd.formeditor.ftl.parameter.ParameterType;
import ru.runa.gpd.formeditor.ftl.parameter.UserTypeVariableListComboParameter;

public class ComponentParameter {
    private final ParameterType type;
    private final String name;
    private final boolean required;
    private final String description;
    private final VariableAccess variableAccess;
    private final String variableTypeFilter;
    private final List<ComboOption> comboOptions = Lists.newArrayList();
    private final List<ComponentParameter> dependents = Lists.newArrayList();

    public ComponentParameter(IConfigurationElement element) {
        this.type = ComponentTypeRegistry.getParameterNotNull(element.getAttribute("type"));
        this.required = !Boolean.valueOf(element.getAttribute("optional"));
        this.name = element.getAttribute("name");
        this.description = element.getAttribute("description");
        this.variableAccess = VariableAccess.valueOf(element.getAttribute("variableAccess"));
        if (type instanceof UserTypeVariableListComboParameter) {
            this.variableTypeFilter = List.class.getName();
        } else {
            this.variableTypeFilter = element.getAttribute("variableTypeFilter");
        }
        for (IConfigurationElement optionElement : element.getChildren()) {
            comboOptions.add(new ComboOption(optionElement));
        }
    }

    public ParameterType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return name + (required ? " *" : "");
    }

    public String getDescription() {
        return description;
    }

    public boolean isRequired() {
        return required;
    }

    public VariableAccess getVariableAccess() {
        return variableAccess;
    }

    public String getVariableTypeFilter() {
        return variableTypeFilter;
    }

    public List<ComboOption> getOptions() {
        return comboOptions;
    }

    public List<ComponentParameter> getDependents() {
        return dependents;
    }
}
