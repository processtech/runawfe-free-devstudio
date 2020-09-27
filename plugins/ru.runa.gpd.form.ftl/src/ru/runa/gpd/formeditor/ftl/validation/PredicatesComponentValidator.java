package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.PredicatesParameter;
import ru.runa.gpd.formeditor.ftl.parameter.PredicatesParameter.PredicatesDelegable;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.GraphElementAware;
import ru.runa.gpd.office.store.InternalStorageOperationHandlerCellEditorProvider;

public class PredicatesComponentValidator extends DefaultParameterTypeValidator {
    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, ComponentParameter parameter) {
        final List<ValidationError> errors = super.validate(formNode, component, parameter);
        if (!errors.isEmpty()) {
            return errors;
        }

        final PredicatesDelegable delegable = new PredicatesValidateValue((String) component.getParameterValue(parameter), formNode);
        try {
            new InternalStorageOperationHandlerCellEditorProvider().validateValue(delegable, errors);
        } catch (Exception e) {
            PluginLogger.logError("Error occured during validation", e);
        }
        return errors;
    }

    public static class PredicatesValidateValue extends PredicatesParameter.PredicatesDelegable implements GraphElementAware {
        private final GraphElement graphElement;

        public PredicatesValidateValue(String configuration, GraphElement graphElement) {
            super(null, null, configuration);
            this.graphElement = graphElement;
        }

        @Override
        public GraphElement getGraphElement() {
            return graphElement;
        }

    }
}
