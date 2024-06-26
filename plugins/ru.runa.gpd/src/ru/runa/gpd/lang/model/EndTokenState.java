package ru.runa.gpd.lang.model;

import java.util.List;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.change.ChangeSubprocessDefinitionBehaviorFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.EmbeddedSubprocess.Behavior;
import ru.runa.gpd.lang.model.bpmn.AbstractEndTextDecorated;

public class EndTokenState extends AbstractEndTextDecorated {

    private EndTokenSubprocessDefinitionBehavior subprocessDefinitionBehavior = EndTokenSubprocessDefinitionBehavior.BACK_TO_BASE_PROCESS;

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (getProcessDefinition() instanceof SubprocessDefinition
                && ((SubprocessDefinition) getProcessDefinition()).getBehavior() == Behavior.GraphPart) {
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_BEHAVIOR, Localization.getString("EndTokenState.property.behaviour"),
                    EndTokenSubprocessDefinitionBehavior.getLabels()));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_BEHAVIOR.equals(id)) {
            return subprocessDefinitionBehavior.ordinal();
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_BEHAVIOR.equals(id)) {
            UndoRedoUtil.executeFeature(
                    new ChangeSubprocessDefinitionBehaviorFeature(this, EndTokenSubprocessDefinitionBehavior.values()[(Integer) value]));
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
        firePropertyChange(PROPERTY_BEHAVIOR, old, this.subprocessDefinitionBehavior);
    }

    @Override
    protected void fillCopyCustomFields(GraphElement copy) {
        super.fillCopyCustomFields(copy);
        ((EndTokenState) copy).setSubprocessDefinitionBehavior(getSubprocessDefinitionBehavior());
    }

}
