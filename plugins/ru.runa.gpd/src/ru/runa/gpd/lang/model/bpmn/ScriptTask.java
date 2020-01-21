package ru.runa.gpd.lang.model.bpmn;

import java.util.List;
import java.util.stream.Collectors;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class ScriptTask extends Node implements Delegable, IBoundaryEventContainer, ConnectableViaDottedTransition {
    private boolean isUseExternalStorageOut = false;
    private boolean isUseExternalStorageIn = false;

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    public boolean isUseExternalStorageOut() {
        return isUseExternalStorageOut;
    }

    public void setUseExternalStorageOut(boolean isUseExternalStorageOut) {
        this.isUseExternalStorageOut = isUseExternalStorageOut;
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_OUT, !isUseExternalStorageOut, isUseExternalStorageOut);

        if (this.isUseExternalStorageIn) {
            this.isUseExternalStorageIn = false;
            firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_IN, !isUseExternalStorageIn, isUseExternalStorageIn);
        }
    }

    public boolean isUseExternalStorageIn() {
        return isUseExternalStorageIn;
    }

    public void setUseExternalStorageIn(boolean isUseExternalStorageIn) {
        this.isUseExternalStorageIn = isUseExternalStorageIn;
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_IN, !isUseExternalStorageIn, isUseExternalStorageIn);

        if (this.isUseExternalStorageOut) {
            this.isUseExternalStorageOut = false;
            firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_OUT, !isUseExternalStorageOut, isUseExternalStorageOut);
        }
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if ("delegableEditHandler".equals(name)) {
            return !isUseExternalStorageOut && !isUseExternalStorageIn;
        }
        return super.testAttribute(target, name, value);
    }

    @Override
    public void addLeavingDottedTransition(DottedTransition transition) {
        addChild(transition);
    }

    @Override
    public List<DottedTransition> getLeavingDottedTransitions() {
        return getChildren(DottedTransition.class);
    }

    @Override
    public List<DottedTransition> getArrivingDottedTransitions() {
        return getProcessDefinition().getNodesRecursive().stream().filter(node -> node instanceof ConnectableViaDottedTransition)
                .flatMap(node -> ((ConnectableViaDottedTransition) node).getLeavingDottedTransitions().stream())
                .filter(dottedTransition -> dottedTransition.getTarget().equals(this)).collect(Collectors.toList());
    }
}
