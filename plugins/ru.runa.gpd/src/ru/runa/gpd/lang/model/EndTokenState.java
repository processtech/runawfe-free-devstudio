package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;

public class EndTokenState extends AbstractEndTextDecorated {

    private EndTokenSubprocessDefinitionBehavior subprocessDefinitionBehavior = EndTokenSubprocessDefinitionBehavior.BACK_TO_BASE_PROCESS;

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (getProcessDefinition() instanceof SubprocessDefinition) {
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_END_TOKEN_BEHAVIOR, Localization.getString("EndTokenState.property.behaviour"),
                    EndTokenSubprocessDefinitionBehavior.getLabels()));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_END_TOKEN_BEHAVIOR.equals(id)) {
            return subprocessDefinitionBehavior.ordinal();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_END_TOKEN_BEHAVIOR.equals(id)) {
            setSubprocessDefinitionBehavior(EndTokenSubprocessDefinitionBehavior.values()[(Integer) value]);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    public EndTokenSubprocessDefinitionBehavior getSubprocessDefinitionBehavior() {
        return subprocessDefinitionBehavior;
    }

    public void setSubprocessDefinitionBehavior(EndTokenSubprocessDefinitionBehavior subprocessDefinitionBehavior) {
        EndTokenSubprocessDefinitionBehavior old = this.subprocessDefinitionBehavior;
        this.subprocessDefinitionBehavior = subprocessDefinitionBehavior;
        firePropertyChange(PROPERTY_END_TOKEN_BEHAVIOR, old, this.subprocessDefinitionBehavior);
    }

    @Override
    public Node getCopy(GraphElement parent) {
        EndTokenState copy = (EndTokenState) super.getCopy(parent);
        copy.setSubprocessDefinitionBehavior(getSubprocessDefinitionBehavior());
        return copy;
    }
}
