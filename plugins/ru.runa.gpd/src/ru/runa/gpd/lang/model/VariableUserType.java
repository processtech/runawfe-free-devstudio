package ru.runa.gpd.lang.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.util.EventSupport;
import ru.runa.gpd.util.IOUtils;

public class VariableUserType extends EventSupport implements VariableContainer, PropertyNames, Comparable<VariableUserType> {
    public static final String PREFIX = "usertype:";
    public static final String DELIM = ".";
    public static final String BY_REFERENCE_ID_ATTRIBUTE_NAME = "id";
    public static final String BY_REFERENCE_ID_ATTRIBUTE_FORMAT = "ru.runa.wfe.var.format.LongFormat";

    private String name;
    private final List<Variable> attributes = Lists.newArrayList();
    private ProcessDefinition processDefinition;
    private boolean isStoreInExternalStorage = false;
    private boolean isByReference = false;
    private boolean global;

    public VariableUserType() {
    }

    public VariableUserType(String name) {
        setName(name);
    }

    public VariableUserType(String name, boolean isStoreInExternalStorage) {
        setName(name);
        setStoreInExternalStorage(isStoreInExternalStorage);
    }

    public VariableUserType(String name, boolean isStoreInExternalStorage, boolean isByReference) {
        setName(name);
        setStoreInExternalStorage(isStoreInExternalStorage);
        setByReference(isByReference);
    }

    public ProcessDefinition getProcessDefinition() {
        return processDefinition;
    }

    public void setProcessDefinition(ProcessDefinition processDefinition) {
        this.processDefinition = processDefinition;
    }

    @Override
    protected void firePropertyChange(PropertyChangeEvent event) {
        super.firePropertyChange(event);
        if (processDefinition != null) {
            processDefinition.setDirty();
            for (PropertyChangeListener delegatedListener : processDefinition.delegatedListeners) {
                delegatedListener.propertyChange(event);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        String old = this.name;
        this.name = name;
        firePropertyChange(PROPERTY_NAME, old, name);
    }

    public List<Variable> getAttributes() {
        return attributes;
    }

    public boolean canUseAsAttribute(Variable attribute) {
        if (attribute.isComplex()) {
            return canUseAsAttributeType(attribute.getUserType());
        } else {
            VariableUserType usertype;
            String[] components = attribute.getFormatComponentClassNames();
            for (String component : components) {
                if ((usertype = processDefinition.getVariableUserType(component)) != null && !canUseAsAttributeType(usertype)) {
                    return false;
                }
            }
            return true;
        }
    }

    public boolean canUseAsAttributeType(VariableUserType usertype) {
        if (Objects.equal(name, usertype.name)) {
            return false;
        }

        if (isByReference != usertype.isByReference()) {
            return false;
        }

        for (Variable attribute : usertype.getAttributes()) {
            if (!canUseAsAttribute(attribute)) {
                return false;
            }
        }
        return true;
    }

    public void addAttribute(Variable variable) {
        if (isByReference && BY_REFERENCE_ID_ATTRIBUTE_NAME.equals(variable.getName())) {
            if (!hasIdAttribute()) {
                attributes.add(0, variable);
                variable.setParent(getProcessDefinition());
                firePropertyChange(PROPERTY_CHILDREN_CHANGED, null, variable);
            }
            return;
        }
        attributes.add(variable);
        variable.setParent(getProcessDefinition());
        firePropertyChange(PROPERTY_CHILDREN_CHANGED, null, variable);
    }

    public void changeAttributePosition(Variable attribute, int position) {
        if (isByReference && BY_REFERENCE_ID_ATTRIBUTE_NAME.equals(attribute.getName())) {
            return;
        }
        if (isByReference && position == 0) {
            position = 1;
        }
        if (position != -1 && attributes.remove(attribute)) {
            attributes.add(position, attribute);
            firePropertyChange(PROPERTY_CHILDREN_CHANGED, null, attribute);
        }
    }

    public void removeAttribute(Variable variable) {
        if (isByReference && BY_REFERENCE_ID_ATTRIBUTE_NAME.equals(variable.getName())) {
            return;
        }
        attributes.remove(variable);
        firePropertyChange(PROPERTY_CHILDREN_CHANGED, null, variable);
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        Preconditions.checkArgument(!expandComplexTypes, "Complex type expansion is not supported");
        Preconditions.checkArgument(typeClassNameFilters == null || typeClassNameFilters.length == 0, "Filtering is not supported");
        return getAttributes();
    }

    public boolean isStoreInExternalStorage() {
        return isStoreInExternalStorage;
    }

    public boolean isByReference() {
        return isByReference;
    }

    public void setStoreInExternalStorage(boolean isStoreInExternalStorage) {
        final boolean old = this.isStoreInExternalStorage;
        this.isStoreInExternalStorage = isStoreInExternalStorage;
        firePropertyChange(PROPERTY_STORE_IN_EXTERNAL_STORAGE, old, isStoreInExternalStorage);
    }

    public void setByReference(boolean isByReference) {
        final boolean old = this.isByReference;
        this.isByReference = isByReference;
        if (isByReference) {
            if (hasIdAttribute()) {
                attributes.get(0).setFormat(BY_REFERENCE_ID_ATTRIBUTE_FORMAT);
            } else {
                Variable existingIdAttr = null;
                for (Variable attr : attributes) {
                    if (BY_REFERENCE_ID_ATTRIBUTE_NAME.equals(attr.getName())) {
                        existingIdAttr = attr;
                        break;
                    }
                }
                if (existingIdAttr != null) {
                    attributes.remove(existingIdAttr);
                    existingIdAttr.setFormat(BY_REFERENCE_ID_ATTRIBUTE_FORMAT);
                    attributes.add(0, existingIdAttr);
                } else {
                    Variable idAttr = createIdAttribute();
                    attributes.add(0, idAttr);
                    if (processDefinition != null) {
                        idAttr.setParent(processDefinition);
                    }
                }
            }
        }

        firePropertyChange(PROPERTY_BY_REFERENCE, old, isByReference);
    }

    public boolean hasIdAttribute() {
        return !attributes.isEmpty()
                && BY_REFERENCE_ID_ATTRIBUTE_NAME.equals(attributes.get(0).getName());
    }

    private Variable createIdAttribute() {
        return new Variable(
                BY_REFERENCE_ID_ATTRIBUTE_NAME,
                BY_REFERENCE_ID_ATTRIBUTE_NAME,
                BY_REFERENCE_ID_ATTRIBUTE_FORMAT,
                null);
    }

    public VariableUserType getCopy() {
        return getCopy(this);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof VariableUserType)) {
            return false;
        }
        VariableUserType type = (VariableUserType) obj;
        return Objects.equal(name, type.name) && Objects.equal(attributes, type.attributes);
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
    public int compareTo(VariableUserType o) {
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", name).add("attributes", attributes).toString();
    }

    public VariableUserType getCopy(VariableUserType source) {
        VariableUserType clone = new VariableUserType(source.getName(), source.isStoreInExternalStorage(), source.isByReference());
        for (Variable attribute : source.getAttributes()) {
            if (attribute.isComplex()) {
                clone.addAttribute(
                        new Variable(attribute.getName(), attribute.getScriptingName(), attribute.getFormat(), getCopy(attribute.getUserType())));
            } else {
                clone.addAttribute(new Variable(attribute));
            }
        }
        clone.setGlobal(this.isGlobal());
        return clone;
    }

    public void updateFromGlobalPartition(VariableUserType typeFromGlobalSection) {
        this.isStoreInExternalStorage = typeFromGlobalSection.isStoreInExternalStorage;
        this.isByReference = typeFromGlobalSection.isByReference;
        attributes.clear();
        for (Variable attribute : typeFromGlobalSection.getAttributes()) {
            if (attribute.isComplex()) {
                this.addAttribute(
                        new Variable(attribute.getName(), attribute.getScriptingName(), attribute.getFormat(), getCopy(attribute.getUserType())));
            } else {
                this.addAttribute(new Variable(attribute));
            }
        }
    }

    public VariableUserType getCopyForGlobalPartition() {
        VariableUserType type = new VariableUserType();
        type.isStoreInExternalStorage = this.isStoreInExternalStorage;
        type.isByReference = this.isByReference;

        for (Variable attribute : this.getAttributes()) {
            if (attribute.isComplex()) {
                type.addAttribute(
                        new Variable(attribute.getName(), attribute.getScriptingName(), attribute.getFormat(), getCopy(attribute.getUserType())));
            } else {
                type.addAttribute(new Variable(attribute));
            }
        }
        String name = this.getName();
        if (name.startsWith(IOUtils.GLOBAL_OBJECT_PREFIX)) {
            name = name.substring(IOUtils.GLOBAL_OBJECT_PREFIX.length());
        }
        type.setName(name);
        return type;
    }

}
