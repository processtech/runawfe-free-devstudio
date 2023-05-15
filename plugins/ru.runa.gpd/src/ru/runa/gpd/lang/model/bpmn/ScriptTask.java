package ru.runa.gpd.lang.model.bpmn;

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import com.google.common.base.Strings;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StorageAware;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.property.DelegableClassPropertyDescriptor;
import ru.runa.gpd.settings.PrefConstants;

public class ScriptTask extends Node implements Delegable, IBoundaryEventContainer, ConnectableViaDottedTransition, StorageAware {
    public static final String INTERNAL_STORAGE_HANDLER_CLASS_NAME = "ru.runa.wfe.office.storage.handler.InternalStorageHandler";
    private static final String PROPERTY_DELEGABLE_EDIT_HANDLER = "delegableEditHandler";

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

    @Override
    public boolean isUseExternalStorageOut() {
        return isUseExternalStorageOut;
    }

    public void setUseExternalStorageOut(boolean isUseExternalStorageOut) {
        this.isUseExternalStorageOut = isUseExternalStorageOut;
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_OUT, !isUseExternalStorageOut, isUseExternalStorageOut);

        if (isUseExternalStorageOut && this.isUseExternalStorageIn) {
            this.isUseExternalStorageIn = false;
            firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_IN, !isUseExternalStorageIn, isUseExternalStorageIn);
        }
    }

    @Override
    public boolean isUseExternalStorageIn() {
        return isUseExternalStorageIn;
    }

    public void setUseExternalStorageIn(boolean isUseExternalStorageIn) {
        this.isUseExternalStorageIn = isUseExternalStorageIn;
        firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_IN, !isUseExternalStorageIn, isUseExternalStorageIn);

        if (isUseExternalStorageIn && this.isUseExternalStorageOut) {
            this.isUseExternalStorageOut = false;
            firePropertyChange(PROPERTY_USE_EXTERNAL_STORAGE_OUT, !isUseExternalStorageOut, isUseExternalStorageOut);
        }
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (PROPERTY_DELEGABLE_EDIT_HANDLER.equals(name)) {
            return !isUseExternalStorageOut && !isUseExternalStorageIn;
        }
        return super.testAttribute(target, name, value);
    }

    @Override
    public void addLeavingDottedTransition(DottedTransition transition) {
        addChild(transition);
        if (!isUseExternalStorageOut()) {
            setUseExternalStorageOut(true);
            setDelegationClassName(INTERNAL_STORAGE_HANDLER_CLASS_NAME);
        }
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

    @Override
    public void removeLeavingDottedTransition(DottedTransition transition) {
        removeChild(transition);
        dottedTransitionRemoved(transition);
    }

    @Override
    public void removeArrivingDottedTransition(DottedTransition transition) {
        dottedTransitionRemoved(transition);
    }

    private void dottedTransitionRemoved(DottedTransition transition) {
        setUseExternalStorageIn(false);
        setUseExternalStorageOut(false);
        setDelegationConfiguration("");
        setDelegationClassName(null);
    }

    @Override
    public void addArrivingDottedTransition(DottedTransition transition) {
        transition.setTarget(this);
        if (!isUseExternalStorageIn()) {
            setUseExternalStorageIn(true);
            setDelegationClassName(INTERNAL_STORAGE_HANDLER_CLASS_NAME);
        }
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (isUseExternalStorageIn || isUseExternalStorageOut) {
            descriptors.removeIf(descriptor -> descriptor instanceof DelegableClassPropertyDescriptor);
        }
    }

    public void resetNameToDefault() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String property = store.getString(PrefConstants.P_SCRIPT_TASK_NAME_BEHAVIOR);
        String className = getDelegationClassName();
        String number = " " + getId().substring(getId().indexOf("ID") + 2);
        if (property.endsWith(PrefConstants.P_LANGUAGE_SCRIPT_TASK_HANDLER_CLASS_LABEL)) {
            if (!Strings.isNullOrEmpty(className)) {
                setName(className.substring(className.lastIndexOf(".") + 1) + number);
            }
        } else if (property.endsWith(PrefConstants.P_LANGUAGE_SCRIPT_TASK_HANDLER_LABEL)) {
            HandlerArtifact artifact = HandlerRegistry.getInstance().getArtifact(className);
            if (artifact != null) {
                setName(artifact.getLabel() + number);
            }
        } else if (property.endsWith(PrefConstants.P_LANGUAGE_SCRIPT_TASK_DEFAULT_LABEL)) {
            setName(Localization.getString("label.element.scripttask") + number);
        }
    }
}
