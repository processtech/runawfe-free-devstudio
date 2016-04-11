package ru.runa.gpd.lang.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.collect.Lists;

public class MessagingNode extends Node implements Active {
    protected final List<VariableMapping> variableMappings = new ArrayList<VariableMapping>();
    private static final List<String> SELECTOR_SPECIAL_NAMES = Lists.newArrayList(VariableUtils.CURRENT_PROCESS_ID,
            VariableUtils.CURRENT_PROCESS_DEFINITION_NAME, VariableUtils.CURRENT_NODE_NAME, VariableUtils.CURRENT_NODE_ID);

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variablesList) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variablesList);
        setDirty();
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        int selectorRulesCount = 0;
        List<String> variableNames = getProcessDefinition().getVariableNames(true);
        for (VariableMapping mapping : variableMappings) {
            if (mapping.isPropertySelector()) {
                selectorRulesCount++;
                if (SELECTOR_SPECIAL_NAMES.contains(mapping.getMappedName())) {
                    continue;
                }
                if (VariableUtils.isVariableNameWrapped(mapping.getMappedName())) {
                    String variableName = VariableUtils.unwrapVariableName(mapping.getMappedName());
                    if (!variableNames.contains(variableName)) {
                        errors.add(ValidationError.createLocalizedError(this, "message.processVariableDoesNotExist", variableName));
                    }
                }
                continue;
            }
            if (!variableNames.contains(mapping.getName())) {
                errors.add(ValidationError.createLocalizedError(this, "message.processVariableDoesNotExist", mapping.getName()));
                continue;
            }
        }
        if (selectorRulesCount == 0) {
            errors.add(ValidationError.createLocalizedWarning(this, "message.selectorRulesEmpty"));
        }
    }

    @Override
    public MessagingNode getCopy(GraphElement parent) {
        MessagingNode copy = (MessagingNode) super.getCopy(parent);
        for (VariableMapping mapping : getVariableMappings()) {
            copy.getVariableMappings().add(mapping.getCopy());
        }
        return copy;
    }

}
