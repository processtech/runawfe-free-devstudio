package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.gef.figure.TaskStateFigure;
import ru.runa.gpd.editor.gef.policy.ActionContainerLayoutEditPolicy;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class TaskStateGraphicalEditPart extends SwimlaneNodeEditPart implements ActionsHost {

    @Override
    protected List<? extends Object> getModelChildren() {
        return getModel().getActions();
    }

    @Override
    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ActionContainerLayoutEditPolicy());
    }

    @Override
    public void refreshActionsVisibility(boolean visible) {
        getFigure().getActionsContainer().setVisible(visible);
    }

    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_MINIMAZED_VIEW);
        list.add(PROPERTY_CHILDS_CHANGED);
        list.add(PROPERTY_ASYNC);
    }

    @Override
    public TaskState getModel() {
        return (TaskState) super.getModel();
    }

    @Override
    public TaskStateFigure getFigure() {
        return (TaskStateFigure) super.getFigure();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connectionEditPart) {
        String transitionName = ((Transition) connectionEditPart.getModel()).getName();
        if (PluginConstants.TIMER_TRANSITION_NAME.equals(transitionName)) {
            return getFigure().getTimerConnectionAnchor();
        } else if (PluginConstants.EVENT_TRANSITION_NAME.equals(transitionName)) {
            return getFigure().getEventConnectionAnchor();
        }
        return getFigure().getLeavingConnectionAnchor();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        if (getModel().getTimer() != null && getModel().getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return getFigure().getTimerConnectionAnchor();
        }
        if (getModel().getCatchEventNodes() != null && getModel().getTransitionByName(PluginConstants.EVENT_TRANSITION_NAME) == null) {
            return getFigure().getEventConnectionAnchor();
        }

        return getFigure().getLeavingConnectionAnchor();
    }
}
