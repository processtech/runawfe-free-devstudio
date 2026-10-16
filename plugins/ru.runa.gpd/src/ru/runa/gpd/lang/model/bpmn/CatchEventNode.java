package ru.runa.gpd.lang.model.bpmn;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.IReceiveMessageNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;

public class CatchEventNode extends AbstractEventNode implements IReceiveMessageNode, IBoundaryEventCapable, IBoundaryEventContainer, ConnectableViaDottedTransition, StorageAware, Delegable {

    public static final String CONDITIONAL_EXPRESSION_HANDLER = "ru.runa.wfe.extension.handler.var.ConditionalExpressionHandler";
    public static final String CONDITIONAL_INTERNAL_STORAGE_HANDLER = "ru.runa.wfe.office.storage.handler.ConditionalInternalStorageHandler";

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
        setDelegationClassName(CONDITIONAL_INTERNAL_STORAGE_HANDLER);
    }

    @Override
    public void removeArrivingDottedTransition(DottedTransition transition) {
        if (isConditional()) {
            setDelegationClassName(CONDITIONAL_EXPRESSION_HANDLER);
        } else {
            setDelegationClassName(null);
        }
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
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    public void validateOnEmptyRules(List<ValidationError> errors) {
        if (isConditional() || (getEventNodeType() == EventNodeType.error && getParent() instanceof IBoundaryEventContainer)) {
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
        if (!isConditional() && isUseExternalStorageIn()) {
            errors.add(ValidationError.createLocalizedError(this, "catchEvent.mustBeConditionalWhenConnectedToStorage"));
        }
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
    public boolean isDelegable() {
        return isConditional();
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("delegableEditHandler".equals(name)
                || "delegableEditConfiguration".equals(name)) {
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
