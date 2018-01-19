package ru.runa.gpd.formeditor.ftl.validation;

import com.google.common.base.Strings;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.UserTypeVariableListComboParameter;
import ru.runa.gpd.formeditor.ftl.parameter.VariableComboParameter;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.util.VariableUtils;

/**
 * @author Alekseev Vitaly
 * @since Jan 19, 2018
 */
public class UserTypeAttributeValidator extends UserTypeAttributeListValidator {

    @Override
    protected VariableUserType getUserType(FormNode formNode, Component component) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof UserTypeVariableListComboParameter) {
                return getListVariableUserType(formNode, component, componentParameter);
            } else if (componentParameter.getType() instanceof VariableComboParameter) {
                final String variableName = (String) component.getParameterValue(componentParameter);
                if (Strings.isNullOrEmpty(variableName)) {
                    continue;
                }
                final Variable variable = VariableUtils.getVariableByName(formNode.getProcessDefinition(), variableName);
                if (variable == null) {
                    continue;
                }
                return variable.getUserType();
            }
        }
        return null;
    }
}
