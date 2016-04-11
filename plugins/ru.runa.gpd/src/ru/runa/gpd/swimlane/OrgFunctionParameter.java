package ru.runa.gpd.swimlane;

import ru.runa.gpd.extension.orgfunction.OrgFunctionParameterDefinition;
import ru.runa.gpd.util.VariableUtils;

public class OrgFunctionParameter {
    private final OrgFunctionParameterDefinition definition;
    private String value = "";
    private boolean canBeDeleted = false;
    private boolean variableValue = false;

    public OrgFunctionParameter(OrgFunctionParameterDefinition definition) {
        this.definition = definition;
    }

    public OrgFunctionParameterDefinition getDefinition() {
        return definition;
    }

    public String getValue() {
        return value;
    }

    public String getVariableName() {
        return VariableUtils.unwrapVariableName(value);
    }

    public void setValue(String value) {
        this.value = value;
        variableValue = VariableUtils.isVariableNameWrapped(value);
    }

    public void setVariableValue(String variableName) {
        this.value = VariableUtils.wrapVariableName(variableName);
        this.variableValue = true;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public boolean isVariableValue() {
        return variableValue;
    }

    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
    }
}
