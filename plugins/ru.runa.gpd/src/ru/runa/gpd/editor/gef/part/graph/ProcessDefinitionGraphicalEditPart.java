package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

import ru.runa.gpd.editor.gef.policy.ProcessDefinitionXYLayoutEditPolicy;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class ProcessDefinitionGraphicalEditPart extends ElementGraphicalEditPart {

    @Override
    public ProcessDefinition getModel() {
        return (ProcessDefinition) super.getModel();
    }

    @Override
    protected List<Node> getModelChildren() {
        return getModel().getNodes();
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessDefinitionXYLayoutEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
    }

    @SuppressWarnings("unchecked")
	public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (PROPERTY_NAME.equals(messageId)) {
            refreshVisuals();
        } else if (PROPERTY_CHILDS_CHANGED.equals(messageId)) {
            refreshChildren();
        } else if (PROPERTY_SHOW_ACTIONS.equals(messageId)) {
            Set<EditPart> parts = new HashSet<EditPart>(getViewer().getEditPartRegistry().values());
            for (EditPart part : parts) {
                if (part instanceof ActionsHost) {
                    ((ActionsHost) part).refreshActionsVisibility((Boolean) evt.getNewValue());
                }
            }
        }
    }
}
