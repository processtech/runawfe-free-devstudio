package ru.runa.gpd.lang.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.property.BooleanPropertyDescriptor;
import ru.runa.gpd.property.FormFilesPropertyDescriptor;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinition.Param;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;
import ru.runa.gpd.validation.ValidatorParser;

public abstract class FormNode extends SwimlanedNode {
    public static final String EMPTY = "";
    public static final String VALIDATION_SUFFIX = "validation.xml";
    public static final String SCRIPT_SUFFIX = "js";
    private String formFileName;
    private String formType;
    private boolean useJSValidation;
    private String templateFileName;
    private Boolean reassignSwimlaneToTaskPerformer = null;

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("formExists".equals(name)) {
            return Objects.equal(value, String.valueOf(hasForm()));
        }
        return false;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String type) {
        this.formType = type;
    }

    public boolean hasForm() {
        return formFileName != null && formFileName.length() > 0;
    }

    public boolean hasFormValidation() {
        return getFormValidationFile().exists();
    }
    
    public IFile getFormValidationFile() {
    	return IOUtils.getAdjacentFile(getProcessDefinition().getFile(), getValidationFileName());
    }

    public boolean hasFormScript() {
        return IOUtils.getAdjacentFile(getProcessDefinition().getFile(), getScriptFileName()).exists();
    }

    public String getFormFileName() {
        return formFileName;
    }

    public void setFormFileName(String formFile) {
        String old = this.formFileName;
        this.formFileName = formFile.trim();
        firePropertyChange(PROPERTY_FORM_FILE, old, this.formFileName);
    }

    public String getValidationFileName() {
        return getId() + "." + VALIDATION_SUFFIX;
    }

    public boolean isUseJSValidation() {
        return useJSValidation;
    }

    public void setUseJSValidation(boolean useJSValidation) {
        boolean old = this.useJSValidation;
        this.useJSValidation = useJSValidation;
        firePropertyChange(PROPERTY_FORM_JS_VALIDATION, old, this.useJSValidation);
    }

    public String getScriptFileName() {
        return getId() + "." + SCRIPT_SUFFIX;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        String old = this.templateFileName;
        this.templateFileName = templateFileName;
        firePropertyChange(PROPERTY_FORM_TEMPLATE_FILE, old, this.templateFileName);
    }

    public boolean hasFormTemplate() {
        return templateFileName != null && templateFileName.length() > 0;
    }
    
    public Boolean isReassignSwimlaneToTaskPerformer() {
        return reassignSwimlaneToTaskPerformer;
    }

    public void setReassignSwimlaneToTaskPerformer(Boolean reassignSwimlaneToTaskPerformer) {
        Boolean old = this.reassignSwimlaneToTaskPerformer;
        this.reassignSwimlaneToTaskPerformer = reassignSwimlaneToTaskPerformer;
        firePropertyChange(PROPERTY_SWIMLANE_REASSIGN_TO_TASK_PERFORMER, old, this.reassignSwimlaneToTaskPerformer);
    }
     
    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new FormFilesPropertyDescriptor("formFiles", Localization.getString("FormNode.property.formFiles"), this));
        descriptors.add(new BooleanPropertyDescriptor(PROPERTY_SWIMLANE_REASSIGN_TO_TASK_PERFORMER,
                Localization.getString("Swimlane.reassignSwimlaneToTaskPerformer")));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if ("formFiles".equals(id)) {
            String value = Localization.getString("FormNode.property.formFile") + ": ";
            if (hasForm()) {
                value += FormTypeProvider.getFormType(getFormType()).getName();
            } else {
                value += Localization.getString("no");
            }
            value += ", " + Localization.getString("FormNode.property.formValidation") + ": ";
            value += hasFormValidation() ? Localization.getString("yes") : Localization.getString("no");
            value += ", " + Localization.getString("FormNode.property.formScript") + ": ";
            value += hasFormScript() ? Localization.getString("yes") : Localization.getString("no");
            return value;
        }
        if (PROPERTY_SWIMLANE_REASSIGN_TO_TASK_PERFORMER.equals(id)) {
            return BooleanPropertyDescriptor.Enum.getByValueNotNull(reassignSwimlaneToTaskPerformer).ordinal();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_SWIMLANE_REASSIGN_TO_TASK_PERFORMER.equals(id)) {
            setReassignSwimlaneToTaskPerformer(BooleanPropertyDescriptor.Enum.values()[(Integer) value].getValue());
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        try {
            FormNodeValidation validation = getValidation(definitionFile);
            if (hasFormValidation()) {
                IFile validationFile = IOUtils.getAdjacentFile(definitionFile, getValidationFileName());
                if (!validationFile.exists()) {
                    errors.add(ValidationError.createLocalizedError(this, "formNode.validationFileNotFound", getValidationFileName()));
                    return;
                }
                // check required parameters (#787)
                for (ValidatorConfig config : validation.getGlobalConfigs()) {
                    ValidatorDefinition definition = ValidatorDefinitionRegistry.getDefinition(config.getType());
                    for (Param param : definition.getParams().values()) {
                        String value = config.getParams().get(param.getName());
                        if (param.isRequired() && Strings.isNullOrEmpty(value)) {
                            errors.add(ValidationError.createLocalizedError(this, "formNode.requiredGlobalValidatorParameterIsNotSet",
                                    config.getMessage()));
                        }
                    }
                }
                for (Map.Entry<String, Map<String, ValidatorConfig>> entry : validation.getFieldConfigs().entrySet()) {
                    Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), entry.getKey());
                    if (variable == null) {
                        if (!hasForm()) {
                            errors.add(ValidationError.createLocalizedError(this, "formNode.validationVariableDoesNotExist", entry.getKey()));
                        }
                        // checked later in FormType.validate(...)
                        continue;
                    }
                    for (ValidatorConfig config : entry.getValue().values()) {
                        ValidatorDefinition definition = ValidatorDefinitionRegistry.getDefinition(config.getType());
                        if (definition != null) {
                            if (!definition.isApplicable(variable.getJavaClassName())) {
                                errors.add(ValidationError.createLocalizedError(this, "formNode.validatorVariableIsNotApplicable", config.getType(),
                                        variable.getName(), variable.getFormatLabel()));
                                continue;
                            }
                            for (Param param : definition.getParams().values()) {
                                String value = config.getParams().get(param.getName());
                                if (param.isRequired() && Strings.isNullOrEmpty(value)) {
                                    errors.add(ValidationError.createLocalizedError(this, "formNode.requiredFieldValidatorParameterIsNotSet",
                                            entry.getKey(), definition.getLabel(), param.getLabel()));
                                }
                            }
                        } else {
                            errors.add(ValidationError.createLocalizedError(this, "formNode.validatorIsNotDefined", config.getType()));
                        }
                    }
                }
            }
            if (hasForm()) {
                IFile formFile = IOUtils.getAdjacentFile(definitionFile, this.formFileName);
                FormType formType = FormTypeProvider.getFormType(this.formType);
                byte[] formData = IOUtils.readStreamAsBytes(formFile.getContents(true));
                formType.validate(this, formData, validation, errors);
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Error validating form node: '" + getName() + "'", e);
            errors.add(ValidationError.createLocalizedWarning(this, "formNode.validationUnknownError", e));
        }
    }

    public FormNodeValidation getValidation(IResource resource) {
        if (!hasFormValidation()) {
            return new FormNodeValidation();
        }
        IFolder processFolder = (IFolder) ((resource instanceof IFolder) ? resource : resource.getParent());
        IFile validationFile = IOUtils.getFile(processFolder, getValidationFileName());
        return ValidatorParser.parseValidation(validationFile);
    }

    public Map<String, FormVariableAccess> getFormVariables(IFolder processFolder) throws Exception {
        if (!hasForm()) {
            return Maps.newHashMap();
        }
        FormType formType = FormTypeProvider.getFormType(this.formType);
        IFile formFile = IOUtils.getFile(processFolder, this.formFileName);
        byte[] formData = IOUtils.readStreamAsBytes(formFile.getContents(true));
        return formType.getFormVariableNames(this, formData);
    }

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    protected void fillCopyCustomFields(GraphElement aCopy) {
        super.fillCopyCustomFields(aCopy);
        FormNode copy = (FormNode) aCopy;
        copy.setFormType(getFormType());
        if (hasForm()) {
            copy.setFormFileName(aCopy.getId() + "." + getFormType());
        }
        if (hasFormTemplate()) {
            copy.setTemplateFileName(getTemplateFileName());
        }
        copy.setUseJSValidation(isUseJSValidation());
        copy.setReassignSwimlaneToTaskPerformer(reassignSwimlaneToTaskPerformer);
    }

    @Override
    public List<Variable> getUsedVariables(IFolder processFolder) {
        List<Variable> result = super.getUsedVariables(processFolder);
        try {
            Map<String, FormVariableAccess> variables = getFormVariables(processFolder);
            for (String variableName : variables.keySet()) {
                Variable variable = VariableUtils.getVariableByName(getProcessDefinition(), variableName);
                if (variable != null) {
                    result.add(variable);
                }
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        return result;
    }

}
