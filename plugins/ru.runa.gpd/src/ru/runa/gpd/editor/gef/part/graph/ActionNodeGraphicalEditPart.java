package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import org.eclipse.gef.EditPolicy;

import ru.runa.gpd.editor.gef.figure.ActionNodeFigure;
import ru.runa.gpd.editor.gef.policy.ActionContainerLayoutEditPolicy;
import ru.runa.gpd.lang.model.jpdl.ActionNode;

public class ActionNodeGraphicalEditPart extends LabeledNodeGraphicalEditPart implements ActionsHost {
    @Override
    public ActionNode getModel() {
        return (ActionNode) super.getModel();
    }

    @Override
    public ActionNodeFigure getFigure() {
        return (ActionNodeFigure) super.getFigure();
    }

    @Override
    protected List<? extends Object> getModelChildren() {
        return getModel().getActions();
    }

    @Override
    public void refreshActionsVisibility(boolean visible) {
        getFigure().getActionsContainer().setVisible(visible);
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ActionContainerLayoutEditPolicy());
    }
}
