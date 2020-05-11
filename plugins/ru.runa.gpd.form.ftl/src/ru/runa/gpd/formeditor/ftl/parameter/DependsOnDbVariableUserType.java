package ru.runa.gpd.formeditor.ftl.parameter;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;

public interface DependsOnDbVariableUserType {
    default Optional<ComponentParameter> getDbUserTypeParameter(Component component) {
        return component.getType().getParameters().stream().filter(parameter -> parameter.getType() instanceof DbUserTypeListComboParameter)
                .findFirst();
    }

    default Optional<VariableUserType> getUserType(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        return Optional.ofNullable(processDefinition.getVariableUserType((String) component.getParameterValue(parameter)));
    }

    default Optional<VariableUserType> userType(Component component, ProcessDefinition processDefinition) {
        final Optional<ComponentParameter> userTypeParameter = getDbUserTypeParameter(component);
        if (!userTypeParameter.isPresent()) {
            PluginLogger.logWarnWithDialog(Messages.getString("DbUserTypeParameter.unavailable"));
            return Optional.empty();
        }
        final Optional<VariableUserType> userType = getUserType(component, userTypeParameter.get(), processDefinition);
        if (!userType.isPresent()) {
            PluginLogger.logWarnWithDialog(
                    MessageFormat.format(Messages.getString("DbUserTypeParameter.notSpecified"), userTypeParameter.get().getLabel()));
            return Optional.empty();
        }

        return userType;
    }

    default Optional<VariableUserType> userTypeWithValidation(Component component, ProcessDefinition processDefinition, List<ValidationError> errors,
            GraphElement graphElement) {
        final Optional<ComponentParameter> userTypeParameter = getDbUserTypeParameter(component);
        if (!userTypeParameter.isPresent()) {
            errors.add(ValidationError.createError(graphElement, Messages.getString("DbUserTypeParameter.unavailable")));
            return Optional.empty();
        }
        final Optional<VariableUserType> userType = getUserType(component, userTypeParameter.get(), processDefinition);
        if (!userType.isPresent()) {
            errors.add(ValidationError.createError(graphElement,
                    MessageFormat.format(Messages.getString("DbUserTypeParameter.notSpecified"), userTypeParameter.get().getLabel())));
            return Optional.empty();
        }

        return userType;
    }
}
