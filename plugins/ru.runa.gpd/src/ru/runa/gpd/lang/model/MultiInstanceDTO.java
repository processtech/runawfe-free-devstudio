package ru.runa.gpd.lang.model;

import java.util.List;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.wfe.lang.MultiTaskCreationMode;

public class MultiInstanceDTO {
    private final MultiinstanceParameters parameters;
    private final List<VariableMapping> variableMappings;

    public MultiInstanceDTO(MultiinstanceParameters parameters, List<VariableMapping> variableMappings) {
        this.parameters = parameters;
        this.variableMappings = variableMappings;
    }

    public String getDiscriminatorUsage() {
        return parameters.getDiscriminatorMapping().getUsage();
    }

    public String getDiscriminatorValue() {
        return parameters.getDiscriminatorMapping().getName();
    }

    public MultiTaskCreationMode getCreationMode() {
        return parameters.getCreationMode();
    }

    public String getSwimlaneName() {
        return parameters.getSwimlaneName();
    }

    public String getDiscriminatorCondition() {
        return parameters.getDiscriminatorCondition();
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }
}
