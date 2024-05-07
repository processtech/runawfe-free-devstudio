package ru.runa.gpd.lang.model;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.TooltipBuilderHelper;
import ru.runa.gpd.editor.graphiti.change.ChangeDisableCascadingSuspensionFeature;
import ru.runa.gpd.editor.graphiti.change.ChangeTransactionalFeature;
import ru.runa.gpd.editor.graphiti.change.ChangeUsedSubprocessFeature;
import ru.runa.gpd.editor.graphiti.change.ChangeValidateAtStartFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.SubprocessFinder;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.lang.AsyncCompletionMode;

public class Subprocess extends Node implements Synchronizable, IBoundaryEventContainer, ITimed {
    protected String subProcessName = "";
    protected List<VariableMapping> variableMappings = Lists.newArrayList();
    private boolean embedded;
    private boolean async;
    private boolean transactional;
    protected boolean validateAtStart;
    private boolean disableCascadingSuspension;
    private AsyncCompletionMode asyncCompletionMode = AsyncCompletionMode.ON_MAIN_PROCESS_END;
    public static List<String> PLACEHOLDERS = Lists.newArrayList(VariableUtils.CURRENT_PROCESS_ID, VariableUtils.CURRENT_PROCESS_DEFINITION_NAME,
            VariableUtils.CURRENT_NODE_ID, VariableUtils.CURRENT_NODE_NAME);

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (subProcessName == null || subProcessName.length() == 0) {
            errors.add(ValidationError.createLocalizedError(this, "subprocess.empty"));
            return;
        }
        if (embedded && !(this instanceof EventSubprocess)) {
            if (getLeavingTransitions().size() != 1) {
                errors.add(ValidationError.createLocalizedError(this, "subprocess.embedded.required1leavingtransition"));
            }
        } else {
            ProcessDefinition subprocessDefinition = getSubProcessDefinition();
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
                    if (mapping.isMultiinstanceLink() && mapping.getMappedName().isEmpty()) {
                        errors.add(ValidationError.createLocalizedWarning(this, "subprocess.subProcessVariableIsNotSelected"));
                    } else {
                        errors.add(
                                ValidationError.createLocalizedWarning(this, "subprocess.subProcessVariableDoesNotExist", mapping.getMappedName()));
                    }
                    continue;
                }
                if (!isCompatibleVariables(mapping, processVariable, subprocessVariable)) {
                    VariableFormatArtifact artifact1 = VariableFormatRegistry.getInstance().getArtifactNotNull(processVariable.getFormatClassName());
                    VariableFormatArtifact artifact2 = VariableFormatRegistry.getInstance()
                            .getArtifactNotNull(subprocessVariable.getFormatClassName());
                    errors.add(ValidationError.createLocalizedWarning(this, "subprocess.variableMappingIncompatibleTypes", processVariable.getName(),
                            artifact1.getLabel(), subprocessVariable.getName(), artifact2.getLabel()));
                }
            }
        }
        if (isAsync()) {
            String propertyName = LanguageElementPreferenceNode.getId(this.getTypeDefinition(), getProcessDefinition().getLanguage()) + '.'
                    + PrefConstants.P_LANGUAGE_SUB_PROCESS_ASYNC_INPUT_DATA;
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            boolean inputDataAllowedInAsyncSubProcess = store.contains(propertyName) ? store.getBoolean(propertyName) : false;
            for (VariableMapping mapping : variableMappings) {
                if (isAsync() && mapping.isWritable() && !inputDataAllowedInAsyncSubProcess) {
                    errors.add(ValidationError.createLocalizedWarning(this, "subprocess.asyncVariablesInput"));
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
        if (variable1.getUserType() != null && Objects.equals(variable1.getUserType(), variable2.getUserType())) {
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
        firePropertyChange(PROPERTY_VARIABLES, null, variableMappings);
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
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        return !(this instanceof EventSubprocess);
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0 && !(this instanceof EventSubprocess);
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_SUBPROCESS, Localization.getString("Subprocess.Name")));
        if (isEmbedded()) {
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_TRANSACTIONAL, Localization.getString("Subprocess.Transactional"),
                    YesNoComboBoxTransformer.LABELS));
        } else {
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_VALIDATE_AT_START, Localization.getString("Subprocess.ValidateAtStart"),
                    YesNoComboBoxTransformer.LABELS));
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_DISABLE_CASCADING_SUSPENSION,
                    Localization.getString("Subprocess.DisableCascadingSuspension"), YesNoComboBoxTransformer.LABELS));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SUBPROCESS.equals(id)) {
            return subProcessName;
        }
        if (PROPERTY_TRANSACTIONAL.equals(id)) {
            if (transactional) {
                return Integer.valueOf(0);
            } else {
                return Integer.valueOf(1);
            }
        }
        if (PROPERTY_VALIDATE_AT_START.equals(id)) {
            if (validateAtStart) {
                return Integer.valueOf(0);
            } else {
                return Integer.valueOf(1);
            }
        }
        if (PROPERTY_DISABLE_CASCADING_SUSPENSION.equals(id)) {
            if (disableCascadingSuspension) {
                return Integer.valueOf(0);
            } else {
                return Integer.valueOf(1);
            }
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
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        // we are not copy embedded subprocess
        ((Subprocess) copy).setSubProcessName(embedded ? "" : getSubProcessName());
        for (VariableMapping mapping : getVariableMappings()) {
            ((Subprocess) copy).getVariableMappings().add(mapping.getCopy());
        }
        ((Subprocess) copy).setEmbedded(isEmbedded());
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

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        boolean old = this.transactional;
        this.transactional = transactional;
        firePropertyChange(PROPERTY_TRANSACTIONAL, old, this.transactional);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_SUBPROCESS.equals(id)) {
            SubprocessDTO newSubprocessDTO = new SubprocessDTO(getVariableMappings(), (String) value);
            UndoRedoUtil.executeFeature(new ChangeUsedSubprocessFeature(this, newSubprocessDTO));
        } else if (PROPERTY_TRANSACTIONAL.equals(id)) {
            UndoRedoUtil.executeFeature(new ChangeTransactionalFeature(this, YesNoComboBoxTransformer.setPropertyValue(value)));
        } else if (PROPERTY_VALIDATE_AT_START.equals(id)) {
            UndoRedoUtil.executeFeature(new ChangeValidateAtStartFeature(this, YesNoComboBoxTransformer.setPropertyValue(value)));
        } else if (PROPERTY_DISABLE_CASCADING_SUSPENSION.equals(id)) {
            UndoRedoUtil.executeFeature(new ChangeDisableCascadingSuspensionFeature(this, YesNoComboBoxTransformer.setPropertyValue(value)));
        } else {
            super.setPropertyValue(id, value);
        }
    }

    public boolean isValidateAtStart() {
        return validateAtStart;
    }

    public void setValidateAtStart(boolean validateAtStart) {
        boolean old = this.validateAtStart;
        this.validateAtStart = validateAtStart;
        firePropertyChange(PROPERTY_VALIDATE_AT_START, old, this.validateAtStart);
    }

    public boolean isDisableCascadingSuspension() {
        return disableCascadingSuspension;
    }

    public void setDisableCascadingSuspension(boolean disableCascadingSuspension) {
        boolean old = this.disableCascadingSuspension;
        this.disableCascadingSuspension = disableCascadingSuspension;
        firePropertyChange(PROPERTY_DISABLE_CASCADING_SUSPENSION, old, this.disableCascadingSuspension);
    }

    public ProcessDefinition getSubProcessDefinition() {
        return SubprocessFinder.findSubProcessDefinition(this);
    }

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    protected void appendExtendedTooltip(StringBuilder tooltipBuilder) {
        super.appendExtendedTooltip(tooltipBuilder);
        if (!getVariableMappings().isEmpty()) {
            tooltipBuilder.append(TooltipBuilderHelper.NEW_LINE + TooltipBuilderHelper.SPACE + Localization.getString("property.variable.mappings")
                    + TooltipBuilderHelper.COLON);
            tooltipBuilder.append(TooltipBuilderHelper.variableMappingsToString(getVariableMappings(), true));
        }
    }

}
