package ru.runa.gpd.lang.model;

import java.util.List;
import ru.runa.gpd.util.VariableMapping;

public class MultiSubprocessDTO extends SubprocessDTO {
    private String discriminatorCondition;

    public MultiSubprocessDTO(String discriminatorCondition, List<VariableMapping> variableMappings, String subprocessName) {
        super(variableMappings, subprocessName);
        this.discriminatorCondition = discriminatorCondition;
    }

    @Override
    public String getDiscriminatorCondition() {
        return discriminatorCondition;
    }

}
