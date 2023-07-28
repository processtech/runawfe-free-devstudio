package ru.runa.gpd.lang.model;

import java.util.List;
import java.util.function.Supplier;
import org.eclipse.core.resources.IFile;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.ui.custom.JavaIdentifierChecker;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

public interface VariableMappingsValidator extends VariableMappingsHolder {
    default void validate(List<ValidationError> errors, IFile definitionFile, Supplier<GraphElement> source) {
        int selectorRulesCount = 0;
        List<String> variableNames = getProcessDefinition().getVariableNames(true);
        for (VariableMapping mapping : getVariableMappings()) {
            if (mapping.isPropertySelector()) {
                selectorRulesCount++;
                if (!JavaIdentifierChecker.isValid(mapping.getName())) {
                    errors.add(ValidationError.createLocalizedError(source.get(), "message.invalidSelectorName", mapping.getName()));
                }
                if (VariableUtils.SELECTOR_SPECIAL_NAMES.contains(mapping.getMappedName())) {
                    continue;
                }
                if (VariableUtils.isVariableNameWrapped(mapping.getMappedName())) {
                    String variableName = VariableUtils.unwrapVariableName(mapping.getMappedName());
                    if (!variableNames.contains(variableName)) {
                        errors.add(ValidationError.createLocalizedError(source.get(), "message.processVariableDoesNotExist", variableName));
                    }
                }
                continue;
            }
            if (!variableNames.contains(mapping.getName())) {
                errors.add(ValidationError.createLocalizedError(source.get(), "message.processVariableDoesNotExist", mapping.getName()));
                continue;
            }
        }
        if (selectorRulesCount == 0) {
            validateOnEmptyRules(errors);
        }
    }

    void validateOnEmptyRules(List<ValidationError> errors);

    ProcessDefinition getProcessDefinition();
}
