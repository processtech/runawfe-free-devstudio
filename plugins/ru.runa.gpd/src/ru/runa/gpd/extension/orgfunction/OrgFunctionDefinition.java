package ru.runa.gpd.extension.orgfunction;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.extension.Artifact;

public class OrgFunctionDefinition extends Artifact {
    public static final String MISSED_DEFINITION = "Missed OrgFunction definition";
    public static final OrgFunctionDefinition DEFAULT = new OrgFunctionDefinition("EmptyOrgFunctionName", "", new ArrayList<OrgFunctionParameterDefinition>(), false);
    private final List<OrgFunctionParameterDefinition> parameters;
    private final boolean usedForEscalation;

    public OrgFunctionDefinition(String className, String label, List<OrgFunctionParameterDefinition> parameters, boolean usedForEscalation) {
        super(true, className, label);
        this.parameters = parameters;
        this.usedForEscalation = usedForEscalation;
    }

    protected void checkMultipleParameters() {
        String multipleParamName = null;
        int multipleParamIndex = -1;
        for (int i = 0; i < parameters.size(); i++) {
            OrgFunctionParameterDefinition param = parameters.get(i);
            if (param.isMultiple()) {
                multipleParamIndex = i;
                multipleParamName = param.getName();
                break;
            }
        }
        // Now check all next parameters are with same name.
        // Set to them 'transientParam' property
        if (multipleParamIndex != -1) {
            for (int i = multipleParamIndex + 1; i < parameters.size(); i++) {
                OrgFunctionParameterDefinition param = parameters.get(i);
                if (!param.isMultiple() || !multipleParamName.equals(param.getName())) {
                    throw new RuntimeException("Misconfiguration in orgfunction definition: " + this);
                }
            }
        }
    }

    public List<OrgFunctionParameterDefinition> getParameters() {
        return parameters;
    }

    public OrgFunctionParameterDefinition getParameter(String name) {
        for (OrgFunctionParameterDefinition parameter : parameters) {
            if (name.equals(parameter.getName())) {
                return parameter;
            }
        }
        return null;
    }
    
    public boolean isUsedForEscalation() {
        return usedForEscalation;
    }

}
