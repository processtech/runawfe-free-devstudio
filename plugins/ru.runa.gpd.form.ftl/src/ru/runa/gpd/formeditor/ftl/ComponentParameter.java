package ru.runa.gpd.formeditor.ftl;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;

import ru.runa.gpd.formeditor.ftl.parameter.ParameterType;

import com.google.common.collect.Lists;

public class ComponentParameter {
    private final ParameterType type;
    private final String label;
    private final boolean required;
    private final String description;
    private final VariableAccess variableAccess;
    private final String variableTypeFilter;
    private final List<ComboOption> comboOptions = Lists.newArrayList();

    public ComponentParameter(IConfigurationElement element) {
        this.type = ComponentTypeRegistry.getParameterNotNull(element.getAttribute("type"));
        this.required = !Boolean.valueOf(element.getAttribute("optional"));
        this.label = element.getAttribute("name") + (required ? " *" : "");
        this.description = element.getAttribute("description");
        this.variableAccess = VariableAccess.valueOf(element.getAttribute("variableAccess"));
        this.variableTypeFilter = element.getAttribute("variableTypeFilter");
        for (IConfigurationElement optionElement : element.getChildren()) {
            comboOptions.add(new ComboOption(optionElement));
        }
    }

    public ParameterType getType() {
        return type;
    }

    public String getLabel() {
        return label;
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

}
