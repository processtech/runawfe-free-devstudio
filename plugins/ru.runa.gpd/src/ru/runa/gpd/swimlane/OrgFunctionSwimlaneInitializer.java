package ru.runa.gpd.swimlane;

import java.util.List;

import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionParameterDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class OrgFunctionSwimlaneInitializer extends SwimlaneInitializer {
    private final OrgFunctionDefinition definition;
    private final List<OrgFunctionParameter> parameters = Lists.newArrayList();

    public OrgFunctionSwimlaneInitializer() {
        this(OrgFunctionDefinition.DEFAULT);
    }

    public OrgFunctionSwimlaneInitializer(OrgFunctionDefinition definition) {
        this.definition = definition;
        for (OrgFunctionParameterDefinition parameterDefinition : definition.getParameters()) {
            addParameter(new OrgFunctionParameter(parameterDefinition));
        }
    }

    public OrgFunctionSwimlaneInitializer(String swimlaneConfiguration) {
        int leftBracketIndex = swimlaneConfiguration.indexOf(LEFT_BRACKET);
        int rightBracketIndex = swimlaneConfiguration.indexOf(RIGHT_BRACKET);
        String orgFunctionDefinitionName = swimlaneConfiguration.substring(0, leftBracketIndex);
        definition = OrgFunctionsRegistry.getInstance().getArtifactNotNull(orgFunctionDefinitionName);
        for (OrgFunctionParameterDefinition parameterDefinition : definition.getParameters()) {
            addParameter(new OrgFunctionParameter(parameterDefinition));
        }
        String parametersString = swimlaneConfiguration.substring(leftBracketIndex + 1, rightBracketIndex);
        String[] parameters = parametersString.split(",", -1);
        int definitionParamsSize = getParameters().size();
        if (parameters.length != definitionParamsSize) {
            OrgFunctionParameter lastParameter = getParameters().get(definitionParamsSize - 1);
            if (definitionParamsSize < parameters.length && lastParameter.getDefinition().isMultiple()) {
                // last parameter is multiple
                propagateParameter(lastParameter, parameters.length - definitionParamsSize);
            } else {
                throw new RuntimeException("Unapplicable parameters to org function: " + definition.getName());
            }
        }
        for (int i = 0; i < parameters.length; i++) {
            OrgFunctionParameter functionParameter = getParameters().get(i);
            functionParameter.setValue(parameters[i]);
        }
    }

    public OrgFunctionDefinition getDefinition() {
        return definition;
    }

    public List<OrgFunctionParameter> getParameters() {
        return parameters;
    }

    public OrgFunctionParameter getParameter(String name) {
        for (OrgFunctionParameter parameter : parameters) {
            if (name.equals(parameter.getDefinition().getName())) {
                return parameter;
            }
        }
        return null;
    }

    public void propagateParameter(OrgFunctionParameter parameter, int count) {
        for (int i = 0; i < count; i++) {
            OrgFunctionParameter newParameter = new OrgFunctionParameter(parameter.getDefinition());
            newParameter.setCanBeDeleted(true);
            addParameter(newParameter);
        }
    }

    public void addParameter(OrgFunctionParameter parameter) {
        parameters.add(parameter);
    }

    public void removeParameter(OrgFunctionParameter parameter) {
        if (!parameter.isCanBeDeleted()) {
            throw new RuntimeException("Trying to remove non-transient parameter");
        }
        parameters.remove(parameter);
    }

    @Override
    public void validate(Swimlane swimlane, List<ValidationError> errors) {
        for (OrgFunctionParameter parameter : parameters) {
            String value = parameter.getValue();
            if (value.length() == 0) {
                errors.add(ValidationError.createLocalizedError(swimlane, "orgfunction.emptyParam"));
            } else if (parameter.isVariableValue()) {
                List<String> variableNames = swimlane.getVariableNames(true, parameter.getDefinition().getType());
                if (!variableNames.contains(parameter.getVariableName())) {
                    errors.add(ValidationError.createLocalizedError(swimlane, "orgfunction.varSelectorItemNotExist"));
                }
            }
        }
    }

    @Override
    public boolean hasReference(Variable variable) {
        for (OrgFunctionParameter parameter : parameters) {
            if (parameter.isVariableValue()) {
                if (Objects.equal(parameter.getVariableName(), variable.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onVariableRename(String variableName, String newVariableName) {
        for (OrgFunctionParameter parameter : parameters) {
            if (parameter.isVariableValue() && Objects.equal(parameter.getVariableName(), variableName)) {
                parameter.setVariableValue(newVariableName);
            }
        }
    }

    @Override
    public boolean isValid() {
        throw new UnsupportedOperationException("Implement if you need this");
    }
    
    @Override
    public SwimlaneInitializer getCopy() {
        throw new UnsupportedOperationException("Implement if you need this");
    }
    
    @Override
    public String toString() {
        if (OrgFunctionDefinition.DEFAULT == definition) {
            // special case without initializer
            return "";
        }
        StringBuffer result = new StringBuffer();
        result.append(definition.getName()).append("(");
        boolean first = true;
        for (OrgFunctionParameter parameter : parameters) {
            if (!first) {
                result.append(",");
            }
            first = false;
            result.append(parameter.getValue());
        }
        result.append(")");
        return result.toString();
    }
}
