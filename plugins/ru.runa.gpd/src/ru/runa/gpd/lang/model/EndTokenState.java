package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;

import ru.runa.wfe.definition.ProcessDefinitionAccessType;


public class EndTokenState extends AbstractEndTextDecorated {
    
    private EndProcessBehavior endProcessBehavior = EndProcessBehavior.BACK_TO_BASE_PROCESS;
            
    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (ProcessDefinitionAccessType.EmbeddedSubprocess == getProcessDefinition().getAccessType()) {
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_END_PROCESS_BEHAVIOR, 
                    Localization.getString("EndState.property." + PROPERTY_END_PROCESS_BEHAVIOR), 
                    EndProcessBehavior.getLabels()));
        }
    }
    
    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_END_PROCESS_BEHAVIOR.equals(id)) {
            return endProcessBehavior.ordinal();
        }
        return super.getPropertyValue(id);
    }
    
    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_END_PROCESS_BEHAVIOR.equals(id)) {
        setEndProcessBehavior(EndProcessBehavior.values()[(Integer) value]);
        } else {
            super.setPropertyValue(id, value);
        }        
    }
    
    public EndProcessBehavior getEndProcessBehavior() {
        return endProcessBehavior;
    }

    public void setEndProcessBehavior(EndProcessBehavior endProcessBehavior) {
        EndProcessBehavior old = this.endProcessBehavior;
        this.endProcessBehavior = endProcessBehavior;
        firePropertyChange(PROPERTY_END_PROCESS_BEHAVIOR, old, this.endProcessBehavior);
    }
}
