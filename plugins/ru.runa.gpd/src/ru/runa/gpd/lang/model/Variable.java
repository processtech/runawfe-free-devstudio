package ru.runa.gpd.lang.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.var.UserTypeMap;

public class Variable extends NamedGraphElement implements Describable {
    public static final String FORMAT_COMPONENT_TYPE_START = "(";
    public static final String FORMAT_COMPONENT_TYPE_END = ")";
    public static final String FORMAT_COMPONENT_TYPE_CONCAT = ", ";
    private static final String ID_NAME = "id";
    private String scriptingName;
    private String format;
    private boolean publicVisibility;
    private boolean editableInChat;
    private String defaultValue;
    private VariableUserType userType;
    private VariableStoreType storeType = VariableStoreType.DEFAULT;
    private boolean global;

    public Variable() {
    }

    public Variable(String name, String scriptingName, String format, VariableUserType userType) {
        super(name);
        setScriptingName(scriptingName);
        setFormat(format);
        setUserType(userType);
    }

    public Variable(Variable variable) {
        this(variable.getName(), variable.getScriptingName(), variable.getFormat(), variable.getUserType());
        setPublicVisibility(variable.isPublicVisibility());
        setEditableInChat(variable.isEditableInChat());
        setDefaultValue(variable.getDefaultValue());
        setStoreType(variable.getStoreType());
    }

    public Variable(String name, String scriptingName, Variable variable) {
        this(name, scriptingName, variable.getFormat(), variable.getUserType());
        setPublicVisibility(variable.isPublicVisibility());
        setEditableInChat(variable.isEditableInChat());
        setDefaultValue(variable.getDefaultValue());
        setStoreType(variable.getStoreType());
    }

    public VariableUserType getUserType() {
        return userType;
    }

    public void setUserType(VariableUserType userType) {
        this.userType = userType;
        if (userType != null) {
            setFormat(VariableUserType.PREFIX + userType.getName());
        }
    }

    @Override
    protected boolean canNameBeSetFromProperties() {
        return false;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public void setScriptingName(String scriptingName) {
        this.scriptingName = scriptingName;
    }

    @Override
    public void setName(String name) {
        if (name.trim().length() == 0) {
            return;
        }
        if (getProcessDefinition() != null && getProcessDefinition().getVariableNames(true).contains(name)) {
            return;
        }
        super.setName(name);
    }

    public String getFormat() {
        return format;
    }

    public String getFormatClassName() {
        if (format.contains(FORMAT_COMPONENT_TYPE_START)) {
            int index = format.indexOf(FORMAT_COMPONENT_TYPE_START);
            return format.substring(0, index);
        }
        return format;
    }

    public String[] getFormatComponentClassNames() {
        if (format.contains(FORMAT_COMPONENT_TYPE_START)) {
            int index = format.indexOf(FORMAT_COMPONENT_TYPE_START);
            String raw = format.substring(index + 1, format.length() - 1);
            return raw.split(FORMAT_COMPONENT_TYPE_CONCAT, -1);
        }
        return new String[0];
    }

    public String getFormatLabel() {
        if (isComplex()) {
            return getUserType().getName();
        }
        if (format.contains(FORMAT_COMPONENT_TYPE_START)) {
            String label = LocalizationRegistry.getLabel(getFormatClassName()) + FORMAT_COMPONENT_TYPE_START;
            String[] componentClassNames = getFormatComponentClassNames();
            for (int i = 0; i < componentClassNames.length; i++) {
                if (i != 0) {
                    label += FORMAT_COMPONENT_TYPE_CONCAT;
                }
                label += LocalizationRegistry.getLabel(componentClassNames[i]);
            }
            return label + FORMAT_COMPONENT_TYPE_END;
        }
        return LocalizationRegistry.getLabel(format);
    }

    public boolean isComplex() {
        return userType != null;
    }

    public String getJavaClassName() {
        if (isComplex()) {
            return UserTypeMap.class.getName();
        }
        return VariableFormatRegistry.getInstance().getArtifactNotNull(getFormatClassName()).getJavaClassName();
    }

    public void setFormat(String format) {
        String old = this.format;
        this.format = format;
        firePropertyChange(PROPERTY_FORMAT, old, this.format);
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public void setPublicVisibility(boolean publicVisibility) {
        boolean old = this.publicVisibility;
        this.publicVisibility = publicVisibility;
        firePropertyChange(PROPERTY_PUBLIC_VISIBILITY, old, this.publicVisibility);
    }

    public boolean isEditableInChat() {
        return editableInChat;
    }

    public void setEditableInChat(boolean editableInChat) {
        boolean old = this.editableInChat;
        this.editableInChat = editableInChat;
        firePropertyChange(PROPERTY_EDITABLE_IN_CHAT, old, this.editableInChat);
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        String old = this.defaultValue;
        this.defaultValue = defaultValue;
        firePropertyChange(PROPERTY_DEFAULT_VALUE, old, this.defaultValue);
    }

    public VariableStoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(VariableStoreType storeType) {
        VariableStoreType old = this.storeType;
        this.storeType = storeType != null ? storeType : VariableStoreType.DEFAULT;
        firePropertyChange(PROPERTY_STORE_TYPE, old, this.storeType);
    }

    @Override
    public void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        descriptors.add(new PropertyDescriptor(PROPERTY_SCRIPTING_NAME, Localization.getString("Variable.property.scriptingName")));
        fillCustomPropertyDescriptors(descriptors);
    }

    protected void fillCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        descriptors.add(new PropertyDescriptor(PROPERTY_FORMAT, Localization.getString("Variable.property.format")));
        descriptors.add(new PropertyDescriptor(PROPERTY_PUBLIC_VISIBILITY, Localization.getString("Variable.property.publicVisibility")));
        descriptors.add(new PropertyDescriptor(PROPERTY_EDITABLE_IN_CHAT, Localization.getString("Variable.property.editableInChat")));
        descriptors.add(new PropertyDescriptor(PROPERTY_DEFAULT_VALUE, Localization.getString("Variable.property.defaultValue")));
        descriptors.add(new PropertyDescriptor(PROPERTY_STORE_TYPE, Localization.getString("Variable.property.storeType")));
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SCRIPTING_NAME.equals(id)) {
            return getScriptingName();
        }
        if (PROPERTY_FORMAT.equals(id)) {
            return getFormatLabel();
        }
        if (PROPERTY_PUBLIC_VISIBILITY.equals(id)) {
            return publicVisibility ? Localization.getString("yes") : Localization.getString("false");
        }
        if (PROPERTY_EDITABLE_IN_CHAT.equals(id)) {
            return editableInChat ? Localization.getString("yes") : Localization.getString("false");
        }
        if (PROPERTY_DEFAULT_VALUE.equals(id)) {
            return defaultValue == null ? "" : defaultValue;
        }
        if (PROPERTY_STORE_TYPE.equals(id)) {
            return getStoreType() != null ? getStoreType() : VariableStoreType.DEFAULT;
        }
        return super.getPropertyValue(id);
    }

    @Override
    public Image getEntryImage() {
        return SharedImages.getImage("icons/obj/variable.gif");
    }

    @Override
    public String toString() {
        ToStringHelper helper = MoreObjects.toStringHelper(getClass()).add("name", getName());
        if (isComplex()) {
            helper.add("type", userType);
        } else {
            helper.add("format", format);
        }
        return helper.toString();
    }

    @Override
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        Variable copyVariable = (Variable) copy;
        copyVariable.setFormat(getFormat());
        if (getUserType() != null) {
            copyVariable.setUserType(copyVariable.getProcessDefinition().getVariableUserType(getUserType().getName()));
        }
        copyVariable.setScriptingName(getScriptingName());
        copyVariable.setDefaultValue(getDefaultValue());
        copyVariable.setPublicVisibility(isPublicVisibility());
        copyVariable.setEditableInChat(isEditableInChat());
        copyVariable.setStoreType(getStoreType());
        copyVariable.setGlobal(isGlobal());
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        if (this.global != global) {
            this.global = global;
            firePropertyChange(PROPERTY_GLOBAL, !this.global, this.global);
        }
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            return getName().equals(((Variable) obj).getName());
        }
        return super.equals(obj);
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        Map<String, String> undeclaredTypesMap = new TreeMap<String, String>();
        checkVariableType(this, getVariables(true, true), undeclaredTypesMap);
        if (getName().equals(ID_NAME)) {
            errors.add(ValidationError.createLocalizedError(this, "variable.invalidIdName"));
        }
        undeclaredTypesMap.entrySet().stream()
                .forEach(entry -> errors.add(ValidationError.createLocalizedError(this, "unknown.variableType", entry.getValue(), entry.getKey())));
    }

    private void checkVariableType(Variable variable, List<Variable> allVaribales, Map<String, String> undeclaredTypesMap) {
        VariableUserType userType = variable.getUserType();
        String format = variable.getFormat();
        if (format.startsWith(VariableUserType.PREFIX) && null == userType) {
            undeclaredTypesMap.put(variable.getName(), getName());
        }
        if (null == userType && VariableUtils.isContainerVariable(variable)) {
            String embeddedTypeName = VariableUtils.getListVariableComponentFormat(variable);
            userType = getProcessDefinition().getVariableUserType(embeddedTypeName);
            Class<?> objectClass = null;
            try {
                objectClass = Class.forName(embeddedTypeName);
            } catch (ClassNotFoundException e) {
                objectClass = null;
            }

            if (null == objectClass && null == userType) {
                undeclaredTypesMap.put(embeddedTypeName, getName());
            }
        }
        if (null != userType) {
            List<Variable> attributesList = userType.getAttributes();
            for (Variable attributeVariable : attributesList) {
                checkVariableType(attributeVariable, allVaribales, undeclaredTypesMap);
            }
        }
    }

    public void updateFromGlobalPartition(Variable variableFromGlobalSection) {

        if (variableFromGlobalSection.getUserType() != null) {
            if (this.getUserType() == null) {
                this.getProcessDefinition().addGlobalType(variableFromGlobalSection.getUserType());
                this.setUserType(this.getProcessDefinition()
                        .getVariableUserType(IOUtils.GLOBAL_OBJECT_PREFIX + variableFromGlobalSection.getUserType().getName()));
            } else {
                this.setUserType(this.getProcessDefinition()
                        .getVariableUserType(IOUtils.GLOBAL_OBJECT_PREFIX + variableFromGlobalSection.getUserType().getName()));
            }
        } else {
            this.setUserType(null);
            this.setFormat(variableFromGlobalSection.getFormat());
        }
        this.setDefaultValue(variableFromGlobalSection.getDefaultValue());
        this.setPublicVisibility(variableFromGlobalSection.isPublicVisibility());
        this.setStoreType(variableFromGlobalSection.getStoreType());
    }

    public Variable getCopyForGlobalPartition() {
        Variable variable = new Variable();
        if (this.getUserType() != null) {
            variable.setUserType(this.getUserType());

        }
        variable.setFormat(this.getFormat());
        variable.setDefaultValue(this.getDefaultValue());
        variable.setPublicVisibility(this.isPublicVisibility());
        variable.setStoreType(this.getStoreType());
        String name = this.getName();
        if (name.startsWith(IOUtils.GLOBAL_OBJECT_PREFIX)) {
            name = name.substring(IOUtils.GLOBAL_OBJECT_PREFIX.length());
        }
        variable.setName(name);
        return variable;
    }
}
