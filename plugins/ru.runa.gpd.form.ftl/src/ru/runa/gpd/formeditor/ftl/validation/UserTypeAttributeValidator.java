package ru.runa.gpd.formeditor.ftl.validation;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.UserTypeVariableListComboParameter;
import ru.runa.gpd.formeditor.ftl.parameter.VariableComboParameter;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.VariableUserType;

/**
 * @author Alekseev Vitaly
 * @since Jan 19, 2018
 */
public class UserTypeAttributeValidator extends UserTypeAttributeListValidator {

    @Override
    protected VariableUserType getUserType(FormNode formNode, Component component) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof UserTypeVariableListComboParameter
                    || componentParameter.getType() instanceof VariableComboParameter) {
                return getVariableUserType(formNode, component, componentParameter);
            }
        }
        return null;
    }
}
