package ru.runa.gpd.lang.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.TextDecoratorEmulation;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.bpmn.ConnectableViaDottedTransition;
import ru.runa.gpd.lang.model.bpmn.DataStore;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;
import ru.runa.gpd.lang.model.bpmn.StartEventType;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class StartState extends FormNode implements ConnectableViaDottedTransition, Delegable, HasTextDecorator, StorageAware, VariableMappingsValidator {

    private static final String CONDITIONAL_INTERNAL_STORAGE_HANDLER = "ru.runa.wfe.office.storage.handler.StartInternalStorageHandler";

    protected TextDecoratorEmulation decoratorEmulation;
    protected String timerEventDefinition;

    public StartState() {
        decoratorEmulation = new TextDecoratorEmulation(this);
    }

    @Override
    public boolean isUseExternalStorageIn() {
        return isConnectedToExternalStorageIn();
    }

    @Override
    public boolean isUseExternalStorageOut() {
        return false;
    }

    @Override
    public boolean canAddLeavingDottedTransition() {
        return false;
    }

    @Override
    public boolean canAddArrivingDottedTransition(ConnectableViaDottedTransition source) {
        return source instanceof DataStore
                && ConnectableViaDottedTransition.super.canAddArrivingDottedTransition(source);
    }

    @Override
    public void addArrivingDottedTransition(DottedTransition transition) {
        setSwimlane(null);
        setEventType(StartEventType.conditional);
        transition.setTarget(this);
        setDelegationClassName(CONDITIONAL_INTERNAL_STORAGE_HANDLER);
    }

    @Override
    public void removeArrivingDottedTransition(DottedTransition transition) {
//        setDelegationClassName(null);
    }

    @Override
    public void addLeavingDottedTransition(DottedTransition transition) {
        // forbidden
    }

    @Override
    public void removeLeavingDottedTransition(DottedTransition transition) {
        // impossible
    }

    @Override
    public List<DottedTransition> getLeavingDottedTransitions() {
        return Collections.emptyList();
    }

    @Override
    public List<DottedTransition> getArrivingDottedTransitions() {
        return getProcessDefinition().getNodesRecursive().stream()
                .filter(n -> n instanceof ConnectableViaDottedTransition)
                .flatMap(n -> ((ConnectableViaDottedTransition) n).getLeavingDottedTransitions().stream())
                .filter(t -> t.getTarget() != null && t.getTarget().equals(this))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSwimlaneDisabled() {
        return getProcessDefinition() instanceof SubprocessDefinition || isStartByEvent();
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (hasForm()) {
            if (getProcessDefinition() instanceof SubprocessDefinition) {
                errors.add(ValidationError.createLocalizedError(this, "startState.formIsNotUsableInEmbeddedSubprocess"));
            } else if (getProcessDefinition().getAccessType() == ProcessDefinitionAccessType.OnlySubprocess) {
                errors.add(ValidationError.createLocalizedWarning(this, "startState.formIsNotUsableInSubprocess"));
            }
        }
        if (hasFormScript()) {
            if (getProcessDefinition() instanceof SubprocessDefinition) {
                errors.add(ValidationError.createLocalizedError(this, "startState.formScriptIsNotUsableInEmbeddedSubprocess"));
            } else if (getProcessDefinition().getAccessType() == ProcessDefinitionAccessType.OnlySubprocess) {
                errors.add(ValidationError.createLocalizedWarning(this, "startState.formScriptIsNotUsableInSubprocess"));
            }
        }
        if (hasFormValidation() && getProcessDefinition() instanceof SubprocessDefinition) {
            errors.add(ValidationError.createLocalizedError(this, "startState.formValidationIsNotUsableInEmbeddedSubprocess"));
        }
        if (isSwimlaneDisabled() && getSwimlane() != null) {
            errors.add(ValidationError.createLocalizedError(this, "startState.swimlaneIsNotUsableInEmbeddedSubprocess"));
        }
        if (isStartByTimer() && hasFormValidation() && getValidation(getProcessDefinition().getFile()).getRequiredVariableNames().size() > 0) {
            errors.add(ValidationError.createLocalizedError(this, "startState.startNodeHasBothTimerDefinitionAndRequiredVariables"));
        }
        if (isStartByTimer() && Strings.isNullOrEmpty(getTimerEventDefinition())) {
            errors.add(ValidationError.createLocalizedError(this, "startState.timerEventNotDefined"));
        }
        if (shouldHaveRoutingRules()) {
            validate(errors, definitionFile, () -> this);
        }
        if (getProcessDefinition() instanceof SubprocessDefinition) {
            if (!((SubprocessDefinition) getProcessDefinition()).isTriggeredByEvent()) {
                if (isStartByEvent()) {
                    errors.add(ValidationError.createLocalizedError(this, "startState.startByEventIsNotUsableInEmbeddedSubprocess"));
                }
            } else {
                if (!isStartByEvent() && !isStartByTimer()) {
                    errors.add(ValidationError.createLocalizedError(this, "startState.eventSubprocessStartStateMustContainEvent"));
                }
            }
        }
    }

    @Override
    public TextDecoratorEmulation getTextDecoratorEmulation() {
        return decoratorEmulation;
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (isProcessDefinitionTriggeredByEvent()) {
            List<String> labels = Lists.newArrayList();
            for (StartEventType type : StartEventType.values()) {
                if (type != StartEventType.blank) {
                    labels.add(type.getLabel());
                }
            }
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_EVENT_TYPE, Localization.getString("property.eventType"),
                    labels.toArray(new String[labels.size()])));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_EVENT_TYPE.equals(id)) {
            return isProcessDefinitionTriggeredByEvent() ? getEventType().ordinal() - 1 : getEventType().ordinal();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_EVENT_TYPE.equals(id)) {
            int index = ((Integer) value).intValue();
            if (isProcessDefinitionTriggeredByEvent()) {
                index += 1;
            } else if (index == 0) {
                getProcessDefinition().setDefaultStartNode(this);
            }
            setEventType(StartEventType.values()[index]);
            setSwimlane(null);
            deleteFiles();
        } else {
            super.setPropertyValue(id, value);
        }
    }

    public boolean isStartByEvent() {
        return eventType != StartEventType.blank;
    }

    public boolean shouldHaveRoutingRules() {
        return eventType == StartEventType.message || eventType == StartEventType.signal || eventType == StartEventType.error
                || eventType == StartEventType.cancel;
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("isEventTypeDefined".equals(name)) {
            return Objects.equal(value, String.valueOf(isStartByEvent()));
        }
        if (PROPERTY_DELEGABLE_EDIT_HANDLER.equals(name)) {
            return false;
        }
        return super.testAttribute(target, name, value);
    }

    public String getTimerEventDefinition() {
        return timerEventDefinition;
    }

    public void setTimerEventDefinition(String timerEventDefinition) {
        if (Objects.equal(timerEventDefinition, this.timerEventDefinition)) {
            String oldTimerEventDefinition = this.timerEventDefinition;
            this.timerEventDefinition = timerEventDefinition;
            firePropertyChange(PROPERTY_TIMER_EVENT_DEFINITION, oldTimerEventDefinition, this.timerEventDefinition);
        }
    }

    public boolean isStartByTimer() {
        return eventType == StartEventType.timer;
    }

    public boolean isStartByCondition() {
        return eventType == StartEventType.conditional;
    }

    private StartEventType eventType = StartEventType.blank;

    public StartEventType getEventType() {
        return eventType;
    }

    public void setEventType(StartEventType eventType) {
        if (eventType != this.eventType) {
            StartEventType old = this.eventType;
            this.eventType = eventType;
            firePropertyChange(PROPERTY_EVENT_TYPE, old, this.eventType);
        }
    }

    private final List<VariableMapping> variableMappings = new ArrayList<VariableMapping>();

    @Override
    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    @Override
    public void setVariableMappings(List<VariableMapping> variablesList) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variablesList);
        setDirty();
    }

    @Override
    public void validateOnEmptyRules(List<ValidationError> errors) {
        errors.add(ValidationError.createLocalizedError(this, "message.selectorRulesEmpty"));
    }

    @Override
    protected void fillCopyCustomFields(GraphElement aCopy) {
        super.fillCopyCustomFields(aCopy);
        StartState copy = (StartState) aCopy;
        copy.setEventType(getEventType());
        if (!copy.isStartByEvent()) {
            StartState defaultStartNode = copy.getProcessDefinition().getDefaultStartNode();
            if (defaultStartNode != null && !copy.equals(defaultStartNode)) {
                copy.setEventType(StartEventType.signal);
                copy.setSwimlane(null);
                copy.setFormFileName("");
                copy.setTemplateFileName(null);
            }
        }
        if (isStartByTimer()) {
            copy.setTimerEventDefinition(getTimerEventDefinition());
        } else if (isStartByEvent()) {
            copy.setVariableMappings(Lists.newArrayList(getVariableMappings()));
        }
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
    public boolean isDelegable() {
        return isStartByCondition();
    }

    private boolean isProcessDefinitionTriggeredByEvent() {
        ProcessDefinition processDefinition = getProcessDefinition();
        return processDefinition instanceof SubprocessDefinition && ((SubprocessDefinition) processDefinition).isTriggeredByEvent();
    }

    public boolean isConnectedToExternalStorageIn() {
        return getArrivingDottedTransitions().stream()
                .anyMatch(transition -> transition.getSource() instanceof DataStore);
    }

}
