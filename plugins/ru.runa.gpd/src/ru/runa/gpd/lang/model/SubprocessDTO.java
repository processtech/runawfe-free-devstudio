package ru.runa.gpd.lang.model;

import java.util.List;
import ru.runa.gpd.util.VariableMapping;

public class SubprocessDTO extends MultiInstanceDTO {
    private String subprocessName;

    public SubprocessDTO(List<VariableMapping> variableMappings, String subprocessName) {
        super(null, variableMappings);
        this.subprocessName = subprocessName;
    }

    public String getSubprocessName() {
        return subprocessName;
    }



}
