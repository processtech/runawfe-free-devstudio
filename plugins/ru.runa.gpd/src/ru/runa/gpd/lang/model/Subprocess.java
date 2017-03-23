package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.lang.AsyncCompletionMode;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class Subprocess extends Node implements Synchronizable, IBoundaryEventContainer {
    protected String subProcessName = "";
    protected List<VariableMapping> variableMappings = Lists.newArrayList();
    private boolean embedded;
    private boolean async;
    private AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.ON_MAIN_PROCESS_END;
    public static List<String> PLACEHOLDERS = Lists.newArrayList(VariableUtils.CURRENT_PROCESS_ID, VariableUtils.CURRENT_PROCESS_DEFINITION_NAME,
            VariableUtils.CURRENT_NODE_ID, VariableUtils.CURRENT_NODE_NAME);
    
    @Override
    public AbstractEventNode getCatchEventNodes() {
        return getFirstChild(AbstractEventNode.class);
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (subProcessName == null || subProcessName.length() == 0) {
            errors.add(ValidationError.createLocalizedError(this, "subprocess.empty"));
            return;
        }
        if (embedded) {
            if (getLeavingTransitions().size() != 1) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.embedded.required1leavingtransition"));
            }
        }
        ProcessDefinition subprocessDefinition = ProcessCache.getFirstProcessDefinition(subProcessName);
        if (subprocessDefinition == null) {
            errors.add(ValidationError.createLocalizedWarning(this, "subprocess.notFound"));
            return;
        }
        for (VariableMapping mapping : variableMappings) {
            if (mapping.isText() || mapping.isMultiinstanceLinkByRelation()) {
                continue;
            }
            if (VariableUtils.isVariableNameWrapped(mapping.getName()) && PLACEHOLDERS.contains(mapping.getName())) {
                continue;
            }
            Variable processVariable = VariableUtils.getVariableByName(getProcessDefinition(), mapping.getName());
            if (processVariable == null) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.processVariableDoesNotExist", mapping.getName()));
                continue;
            }
            if (mapping.isSyncable()) {
                checkSyncModeUsage(errors, mapping, subprocessDefinition);
            }
            Variable subprocessVariable = VariableUtils.getVariableByName(subprocessDefinition, mapping.getMappedName());
            if (subprocessVariable == null) {
                errors.add(ValidationError.createLocalizedWarning(this, "subprocess.subProcessVariableDoesNotExist", mapping.getMappedName()));
                continue;
            }
            if (!isCompatibleVariables(mapping, processVariable, subprocessVariable)) {
                VariableFormatArtifact artifact1 = VariableFormatRegistry.getInstance().getArtifactNotNull(processVariable.getFormatClassName());
                VariableFormatArtifact artifact2 = VariableFormatRegistry.getInstance().getArtifactNotNull(subprocessVariable.getFormatClassName());
                errors.add(ValidationError.createLocalizedWarning(this, "subprocess.variableMappingIncompatibleTypes", processVariable.getName(),
                        artifact1.getLabel(), subprocessVariable.getName(), artifact2.getLabel()));
            }
        }
        if (isAsync()) {
            for (VariableMapping mapping : variableMappings) {
                if (isAsync() && mapping.isWritable()) {
                    errors.add(ValidationError.createLocalizedError(this, "subprocess.variablesInputInAsyncTask"));
                    break;
                }
            }
        }
    }

    protected void checkSyncModeUsage(List<ValidationError> errors, VariableMapping mapping, ProcessDefinition subprocessDefinition) {
        if (subprocessDefinition.getSwimlaneByName(mapping.getMappedName()) != null) {
            errors.add(ValidationError.createLocalizedWarning(this, "subprocess.subProcessSwimlaneSyncNotSupported", mapping.getMappedName()));
        }
    }

    protected boolean isCompatibleVariables(VariableMapping mapping, Variable variable1, Variable variable2) {
        if (variable1.getUserType() != null && Objects.equal(variable1.getUserType(), variable2.getUserType())) {
            return true;
        }
        if (VariableFormatRegistry.isAssignableFrom(variable1.getJavaClassName(), variable2.getJavaClassName())) {
            return true;
        }
        return VariableFormatRegistry.isAssignableFrom(variable2.getJavaClassName(), variable1.getJavaClassName());
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variablesList) {
        this.variableMappings.clear();
        this.variableMappings.addAll(variablesList);
        setDirty();
    }

    public void setSubProcessName(String subProcessName) {
        String old = this.subProcessName;
        this.subProcessName = subProcessName;
        firePropertyChange(PROPERTY_SUBPROCESS, old, this.subProcessName);
    }

    public String getSubProcessName() {
        return subProcessName;
    }

    public SubprocessDefinition getEmbeddedSubprocess() {
        return getProcessDefinition().getMainProcessDefinition().getEmbeddedSubprocessByName(getSubProcessName());
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_SUBPROCESS, Localization.getString("Subprocess.Name")));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SUBPROCESS.equals(id)) {
            return subProcessName;
        }
        return super.getPropertyValue(id);
    }

    public boolean isEmbedded() {
        return embedded;
    }

    public void setEmbedded(boolean embedded) {
        boolean old = this.embedded;
        this.embedded = embedded;
        firePropertyChange(PROPERTY_SUBPROCESS, old, this.embedded);
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public void setAsync(boolean async) {
        if (this.async != async) {
            this.async = async;
            firePropertyChange(PROPERTY_ASYNC, !async, async);
        }
    }

    @Override
    public AsyncCompletionMode getAsyncCompletionMode() {
        return asyncCompletionMode;
    }

    @Override
    public void setAsyncCompletionMode(AsyncCompletionMode asyncCompletionMode) {
        AsyncCompletionMode old = this.asyncCompletionMode;
        this.asyncCompletionMode = asyncCompletionMode;
        firePropertyChange(PROPERTY_ASYNC_COMPLETION_MODE, old, asyncCompletionMode);
    }

    @Override
    public Subprocess getCopy(GraphElement parent) {
        Subprocess copy = (Subprocess) super.getCopy(parent);
        // we are not copy embedded subprocess
        copy.setSubProcessName(embedded ? "" : getSubProcessName());
        for (VariableMapping mapping : getVariableMappings()) {
            copy.getVariableMappings().add(mapping.getCopy());
        }
        copy.setEmbedded(isEmbedded());
        return copy;
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        for (VariableMapping mapping : getVariableMappings()) {
            if (mapping.isText()) {
                continue;
            }
            Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), mapping.getName());
            if (variable != null) {
                result.add(variable);
            }
        }
        return result;
    }

}
