package ru.runa.gpd.lang.model;

import java.util.List;
import ru.runa.gpd.util.VariableMapping;

public interface VariableMappingsHolder {

    List<VariableMapping> getVariableMappings();

    void setVariableMappings(List<VariableMapping> variablesList);

}
