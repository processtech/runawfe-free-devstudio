package ru.runa.gpd.lang.model;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.TooltipBuilderHelper;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.property.DurationPropertyDescriptor;
import ru.runa.gpd.ui.custom.JavaIdentifierChecker;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

public abstract class MessageNode extends Node implements VariableMappingsValidator {
    protected final List<VariableMapping> variableMappings = new ArrayList<>();
    private static final List<String> SELECTOR_SPECIAL_NAMES = Lists.newArrayList(VariableUtils.CURRENT_PROCESS_ID,
            VariableUtils.CURRENT_PROCESS_DEFINITION_NAME, VariableUtils.CURRENT_NODE_NAME, VariableUtils.CURRENT_NODE_ID);
    private Duration ttlDuration = new Duration("0 minutes");

    @Override
    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    @Override
    public void setVariableMappings(List<VariableMapping> variablesList) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variablesList);
        setDirty();
        firePropertyChange(PROPERTY_VARIABLES, null, variableMappings);
    }

    public Duration getTtlDuration() {
        return ttlDuration;
    }

    public void setTtlDuration(Duration ttlDuration) {
        Duration old = this.ttlDuration;
        this.ttlDuration = ttlDuration;
        firePropertyChange(PROPERTY_TTL, old, ttlDuration);
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (this instanceof ISendMessageNode) {
            descriptors.add(new DurationPropertyDescriptor(PROPERTY_TTL, getProcessDefinition(), getTtlDuration(), Localization
                    .getString("property.message.ttl")));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_TTL.equals(id)) {
            return ttlDuration;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_TTL.equals(id)) {
            setTtlDuration((Duration) value);
            return;
        }
        super.setPropertyValue(id, value);
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        int selectorRulesCount = 0;
        List<String> variableNames = getProcessDefinition().getVariableNames(true);
        for (VariableMapping mapping : variableMappings) {
            if (mapping.isPropertySelector()) {
                selectorRulesCount++;
                if (!JavaIdentifierChecker.isValid(mapping.getName())) {
                    errors.add(ValidationError.createLocalizedError(this, "message.invalidSelectorName", mapping.getName()));
                }
                if (VariableUtils.SELECTOR_SPECIAL_NAMES.contains(mapping.getMappedName())) {
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
            validateOnEmptyRules(errors);
        }
    }

    @Override
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        ((MessageNode) copy).setTtlDuration(getTtlDuration());
        for (VariableMapping mapping : getVariableMappings()) {
            ((MessageNode) copy).getVariableMappings().add(mapping.getCopy());
        }
    }

    @Override
    public void validateOnEmptyRules(List<ValidationError> errors) {
        errors.add(ValidationError.createLocalizedWarning(this, "message.selectorRulesEmpty"));
    }

    @Override
    protected void appendExtendedTooltip(StringBuilder tooltipBuilder) {
        super.appendExtendedTooltip(tooltipBuilder);
        List<VariableMapping> selectorMappings = getVariableMappings().stream().filter(m -> m.isPropertySelector()).collect(Collectors.toList());
        if (!selectorMappings.isEmpty()) {
            tooltipBuilder.append(TooltipBuilderHelper.NEW_LINE + TooltipBuilderHelper.SPACE + Localization.getString("property.message.routing.data")
                    + TooltipBuilderHelper.COLON);
            tooltipBuilder.append(TooltipBuilderHelper.variableMappingsToString(selectorMappings, false));
        }
        List<VariableMapping> dataMappings = getVariableMappings().stream().filter(m -> !m.isPropertySelector()).collect(Collectors.toList());
        if (!dataMappings.isEmpty()) {
            tooltipBuilder.append(TooltipBuilderHelper.NEW_LINE + TooltipBuilderHelper.SPACE + Localization.getString("property.message.content.data")
                    + TooltipBuilderHelper.COLON);
            tooltipBuilder.append(TooltipBuilderHelper.variableMappingsToString(dataMappings, false));
        }
    }
}
