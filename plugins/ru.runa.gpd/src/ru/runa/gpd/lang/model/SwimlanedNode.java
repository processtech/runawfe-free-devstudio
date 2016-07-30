package ru.runa.gpd.lang.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Objects;

public abstract class SwimlanedNode extends Node implements PropertyChangeListener {
    private Swimlane swimlane;

    protected boolean isSwimlaneDisabled() {
        return false;
    }

    @Override
    public boolean testAttribute(Object target, String name, String value) {
        if (super.testAttribute(target, name, value)) {
            return true;
        }
        if ("disableSwimlaneSelection".equals(name)) {
            return isSwimlaneDisabled() == Boolean.parseBoolean(value);
        }
        return false;
    }

    @Override
    public void setParent(GraphElement parent) {
        super.setParent(parent);
        getProcessDefinition().addPropertyChangeListener(this);
    }

    public Swimlane getSwimlane() {
        return swimlane;
    }

    public String getSwimlaneName() {
        return swimlane != null ? swimlane.getName() : null;
    }

    public String getSwimlaneLabel() {
        return swimlane != null ? "(" + swimlane.getName() + ")" : "";
    }

    public void setSwimlane(Swimlane swimlane) {
        Swimlane old = this.swimlane;
        if (old != null) {
            old.removePropertyChangeListener(this);
        }
        this.swimlane = swimlane;
        if (this.swimlane != null) {
            this.swimlane.addPropertyChangeListener(this);
        }
        firePropertyChange(PROPERTY_SWIMLANE, old, swimlane);
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (getSwimlane() == null && !isSwimlaneDisabled()) {
            errors.add(ValidationError.createLocalizedError(this, "swimlaneNotSet"));
        }
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (!isSwimlaneDisabled() && getProcessDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
            List<String> swimlaneNames = VariableUtils.getVariableNames(getProcessDefinition().getSwimlanes());
            String[] array = swimlaneNames.toArray(new String[swimlaneNames.size()]);
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_SWIMLANE, Localization.getString("SwimlanedNode.property.swimlane"), array));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_SWIMLANE.equals(id)) {
            Swimlane swimlane = getSwimlane();
            if (swimlane == null) {
                return -1;
            }
            List<Swimlane> swimlanes = getProcessDefinition().getSwimlanes();
            return swimlanes.indexOf(swimlane);
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_SWIMLANE.equals(id)) {
            int i = ((Integer) value).intValue();
            setSwimlane(getProcessDefinition().getSwimlanes().get(i));
        } else {
            super.setPropertyValue(id, value);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (PROPERTY_NAME.equals(propertyName) && evt.getSource() instanceof Swimlane) {
            setSwimlane((Swimlane) evt.getSource());
        } else if (NODE_REMOVED.equals(propertyName) && Objects.equal(evt.getOldValue(), getSwimlane())) {
            setSwimlane(null);
        }
    }
}
