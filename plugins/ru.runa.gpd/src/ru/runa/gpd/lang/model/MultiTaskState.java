package ru.runa.gpd.lang.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.bpmn.IMultiInstancesContainer;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.lang.MultiTaskCreationMode;
import ru.runa.wfe.lang.MultiTaskSynchronizationMode;
import ru.runa.wfe.var.format.ExecutorFormat;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MultiTaskState extends TaskState implements IMultiInstancesContainer {
    public static final String USAGE_DEFAULT = VariableMapping.USAGE_MULTIINSTANCE_LINK + ", " + VariableMapping.USAGE_DISCRIMINATOR_VARIABLE;
    private String discriminatorUsage = USAGE_DEFAULT;
    private String discriminatorValue;
    private String discriminatorCondition;
    private MultiTaskSynchronizationMode synchronizationMode = MultiTaskSynchronizationMode.LAST;
    private MultiTaskCreationMode creationMode = MultiTaskCreationMode.BY_DISCRIMINATOR;
    private final List<VariableMapping> variableMappings = Lists.newArrayList();

    @Override
    protected boolean isSwimlaneDisabled() {
        return creationMode == MultiTaskCreationMode.BY_EXECUTORS
                || discriminatorUsage.contains(VariableMapping.USAGE_DISCRIMINATOR_GROUP)
                || discriminatorUsage.contains(VariableMapping.USAGE_DISCRIMINATOR_RELATION);
    }

    public VariableMapping getDiscriminatorMapping() {
        return new VariableMapping(getDiscriminatorValue(), null, getDiscriminatorUsage());
    }

    public MultiinstanceParameters getMultiinstanceParameters() {
        VariableMapping mapping = getDiscriminatorMapping();
        MultiinstanceParameters parameters = new MultiinstanceParameters(Lists.newArrayList(mapping));
        parameters.setCreationMode(creationMode);
        parameters.setSwimlaneName(getSwimlaneName());
        parameters.setDiscriminatorCondition(getDiscriminatorCondition());
        return parameters;
    }

    public String getDiscriminatorUsage() {
        return discriminatorUsage;
    }

    public void setDiscriminatorUsage(String discriminatorUsage) {
        String old = this.discriminatorUsage;
        this.discriminatorUsage = discriminatorUsage;
        firePropertyChange(PROPERTY_DISCRIMINATOR_USAGE, old, discriminatorUsage);
    }

    public String getDiscriminatorValue() {
        return discriminatorValue;
    }

    public void setDiscriminatorValue(String discriminatorValue) {
        String old = this.discriminatorValue;
        this.discriminatorValue = discriminatorValue;
        firePropertyChange(PROPERTY_DISCRIMINATOR_VALUE, old, discriminatorValue);
        if (!isSwimlaneDisabled()) {
            firePropertyChange(PROPERTY_SWIMLANE, old, discriminatorValue);
        }
    }

    public String getDiscriminatorCondition() {
        return discriminatorCondition;
    }

    public void setDiscriminatorCondition(String discriminatorCondition) {
        String old = this.discriminatorCondition;
        this.discriminatorCondition = discriminatorCondition;
        firePropertyChange(PROPERTY_DISCRIMINATOR_CONDITION, old, discriminatorCondition);
    }

    @Override
    public String getSwimlaneLabel() {
        if (isSwimlaneDisabled()) {
            return "";
        }
        return super.getSwimlaneLabel();
    }

    public MultiTaskCreationMode getCreationMode() {
        return creationMode;
    }

    public void setCreationMode(MultiTaskCreationMode creationMode) {
        MultiTaskCreationMode old = this.creationMode;
        this.creationMode = creationMode;
        firePropertyChange(PROPERTY_MULTI_TASK_CREATION_MODE, old, creationMode);
    }

    public MultiTaskSynchronizationMode getSynchronizationMode() {
        return synchronizationMode;
    }

    public void setSynchronizationMode(MultiTaskSynchronizationMode synchronizationMode) {
        MultiTaskSynchronizationMode old = this.synchronizationMode;
        this.synchronizationMode = synchronizationMode;
        firePropertyChange(PROPERTY_MULTI_TASK_SYNCHRONIZATION_MODE, old, synchronizationMode);
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variableMappings) {
        if (!Objects.equal(this.variableMappings, variableMappings)) {
            this.variableMappings.clear();
            this.variableMappings.addAll(variableMappings);
            firePropertyChange(PROPERTY_VARIABLES, null, variableMappings);
        }
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (Strings.isNullOrEmpty(discriminatorValue)) {
            errors.add(ValidationError.createLocalizedError(this, "multiTask.discriminator.empty"));
            return;
        }
        if (creationMode == MultiTaskCreationMode.BY_EXECUTORS
                && VariableMapping.hasUsage(discriminatorUsage, VariableMapping.USAGE_DISCRIMINATOR_VARIABLE)) {
            Variable variable = VariableUtils.getVariableByName(this, discriminatorValue);
            String format = VariableUtils.getListVariableComponentFormat(variable);
            if (!VariableFormatRegistry.isAssignableFrom(ExecutorFormat.class, format)) {
                errors.add(ValidationError.createLocalizedError(this, "multiTask.discriminator.executors.classcasterror"));
            }
        }
        Set<String> names = Sets.newHashSet();
        Set<String> mappedNames = Sets.newHashSet();
        for (VariableMapping mapping : variableMappings) {
            if (names.contains(mapping.getName())) {
                errors.add(ValidationError.createLocalizedError(this, "multiTask.variable.duplicate.name", mapping.getName()));
                return;
            }
            if (mappedNames.contains(mapping.getMappedName())) {
                errors.add(ValidationError.createLocalizedError(this, "multiTask.variable.duplicate.mappedName", mapping.getMappedName()));
                return;
            }
            names.add(mapping.getName());
            mappedNames.add(mapping.getMappedName());
            Variable processVariable = VariableUtils.getVariableByName(getProcessDefinition(), mapping.getName());
            if (processVariable == null) {
                errors.add(ValidationError.createLocalizedError(this, "multiTask.variable.doesNotExist", mapping.getName()));
                continue;
            }
            if (!VariableFormatRegistry.isApplicable(processVariable, List.class.getName())) {
                errors.add(ValidationError.createLocalizedError(this, "multiTask.variable.notListFormat", mapping.getName()));
                continue;
            }
        }
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        List<Variable> result = super.getVariables(expandComplexTypes, includeSwimlanes, typeClassNameFilters);
        Map<String, Variable> processVariables = Maps.newHashMap();
        for (Variable variable : result) {
            processVariables.put(variable.getName(), variable);
        }
        List<Variable> formVariables = Lists.newArrayList();
        for (VariableMapping mapping : getVariableMappings()) {
            Variable listVariable = processVariables.get(mapping.getName());
            if (listVariable == null) {
                continue;
            }
            String componentFormat = VariableUtils.getListVariableComponentFormat(listVariable);
            VariableUserType userType = getProcessDefinition().getVariableUserType(componentFormat);
            Variable variable = new Variable(mapping.getMappedName(), mapping.getMappedName(), componentFormat, userType, true, null);
            if (expandComplexTypes && variable.isComplex()) {
                formVariables.addAll(VariableUtils.expandComplexVariable(variable, variable));
            } else {
                formVariables.add(variable);
            }
        }
        for (Variable variable : formVariables) {
            if (VariableFormatRegistry.isApplicable(variable, typeClassNameFilters)) {
                result.add(variable);
            }
        }
        return result;
    }

    @Override
    public MultiTaskState makeCopy(GraphElement parent) {
        MultiTaskState copy = (MultiTaskState) super.makeCopy(parent);
        copy.setDiscriminatorUsage(discriminatorUsage);
        copy.setDiscriminatorValue(discriminatorValue);
        copy.setDiscriminatorCondition(discriminatorCondition);
        copy.setCreationMode(creationMode);
        copy.setSynchronizationMode(synchronizationMode);
        for (VariableMapping mapping : getVariableMappings()) {
            copy.getVariableMappings().add(mapping.getCopy());
        }
        return copy;
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        if (discriminatorUsage.contains(VariableMapping.USAGE_DISCRIMINATOR_VARIABLE)) {
            Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), discriminatorValue);
            if (variable != null) {
                result.add(variable);
            }
        }
        return result;
    }

}
