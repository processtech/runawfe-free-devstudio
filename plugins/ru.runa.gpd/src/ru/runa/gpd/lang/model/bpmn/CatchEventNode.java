package ru.runa.gpd.lang.model.bpmn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.IReceiveMessageNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.property.DelegableClassPropertyDescriptor;

public class CatchEventNode extends AbstractEventNode implements IReceiveMessageNode, IBoundaryEventCapable, IBoundaryEventContainer, ConnectableViaDottedTransition, StorageAware, Delegable {

    private static final String INTERNAL_STORAGE_DELEGATION_CLASS_NAME = "ru.runa.wfe.office.storage.handler.ConditionalInternalStorageHandler";
    private static final String CONDITIONAL_EXPRESSION_DELEGATION_CLASS_NAME = "ru.runa.wfe.extension.handler.var.ConditionalExpressionHandler";

    public static boolean isBoundaryEventInParent(GraphElement parent) {
        return parent instanceof IBoundaryEventContainer && !(parent.getParent() instanceof IBoundaryEventContainer);
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
        if (!isConditional()) {
            setEventNodeType(EventNodeType.conditional);
        }
        transition.setTarget(this);
        setDelegationClassName(INTERNAL_STORAGE_DELEGATION_CLASS_NAME);
    }

    @Override
    public void removeArrivingDottedTransition(DottedTransition transition) {
        setDelegationClassName(null);
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
    public void setEventNodeType(EventNodeType eventNodeType) {
        if (isUseExternalStorageIn() && eventNodeType != EventNodeType.conditional) {
            return;
        }
        if (!isUseExternalStorageIn() && eventNodeType == EventNodeType.conditional) {
            setDelegationClassName(CONDITIONAL_EXPRESSION_DELEGATION_CLASS_NAME);
        }
        if (eventNodeType != EventNodeType.conditional) {
            setDelegationClassName(null);
        }
        super.setEventNodeType(eventNodeType);
    }

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    public void validateOnEmptyRules(List<ValidationError> errors) {
        if (getEventNodeType() == EventNodeType.error && getParent() instanceof IBoundaryEventContainer) {
            return;
        }
        super.validateOnEmptyRules(errors);
    }

    @Override
    public void updateBoundaryEventConstraint() {
        if (getParent() != null && getParent().getConstraint() != null) {
            getConstraint().setX(getParent().getConstraint().width - getConstraint().width);
            getConstraint().setY(getParent().getConstraint().height - getConstraint().height);
        }
    }

    @Override
    public boolean isBoundaryEvent() {
        return isBoundaryEventInParent(getParent());
    }

    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        if (isBoundaryEvent()) {
            return false;
        }
        return super.allowArrivingTransition(source, transitions);
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (isBoundaryEvent() && getArrivingTransitions().size() > 0) {
            errors.add(ValidationError.createLocalizedError(this, "unresolvedArrivingTransition"));
        }
        if (isConditional()) {
            String handler = getDelegationClassName();
            String config = getDelegationConfiguration();
            if (handler == null || handler.trim().isEmpty()) {
                errors.add(ValidationError.createLocalizedError(this, "delegationClassName.empty"));
            }
            if (config == null || config.trim().isEmpty()) {
                errors.add(ValidationError.createLocalizedError(this, "delegable.invalidConfiguration.empty"));
            }
        } else {
            if (isUseExternalStorageIn()) {
                errors.add(ValidationError.createLocalizedError(this, "catchEvent.mustBeConditionalWhenConnectedToStorage"));
            }
        }
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);

        if (isConditional()) {
            descriptors.removeIf(d -> d instanceof DelegableClassPropertyDescriptor);
        }
    }

    @Override
    public String getDelegationType() {
        return null;
    }

    @Override
    public boolean isDelegable() {
        return isConditional();
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("delegableEditHandler".equals(name)) {
            return false;
        }
        if ("delegableEditConfiguration".equals(name)) {
            return false;
        }
        return super.testAttribute(target, name, value);
    }

    public boolean isConditional() {
        return getEventNodeType() == EventNodeType.conditional;
    }

    public boolean isConnectedToExternalStorageIn() {
        return getArrivingDottedTransitions().stream()
                .anyMatch(transition -> transition.getSource() instanceof DataStore);
    }
}
