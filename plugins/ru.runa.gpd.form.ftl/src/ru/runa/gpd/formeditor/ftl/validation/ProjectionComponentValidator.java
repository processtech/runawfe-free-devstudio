package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;
import java.util.Optional;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.DependsOnDbVariableUserType;
import ru.runa.gpd.formeditor.ftl.parameter.ProjectionParameter.ProjectionDelegable;
import ru.runa.gpd.formeditor.ftl.ui.dialog.projection.ProjectionDialog;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.VariableUserType;

public class ProjectionComponentValidator extends DefaultParameterTypeValidator implements DependsOnDbVariableUserType {
    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        final List<ValidationError> errors = super.validate(formNode, component, parameter);
        if (!errors.isEmpty()) {
            return errors;
        }

        final Optional<VariableUserType> userType = userTypeWithValidation(component, formNode.getProcessDefinition(), errors, formNode);
        if (!userType.isPresent()) {
            return errors;
        }

        final ProjectionDelegable delegable = new ProjectionDelegable(formNode.getProcessDefinition(),
                (String) component.getParameterValue(parameter));
        try {
            new ProjectionDialog(userType.get(), formNode).validateValue(delegable, errors);
        } catch (Exception e) {
            PluginLogger.logError("Error occured during validation", e);
        }
        return errors;
    }
}
