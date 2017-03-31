package ru.runa.gpd.editor.gef.part.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RoutingListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;
import ru.runa.gpd.editor.gef.policy.ActionContainerLayoutEditPolicy;
import ru.runa.gpd.editor.gef.policy.TransitionConnectionBendpointEditPolicy;
import ru.runa.gpd.editor.gef.policy.TransitionConnectionEditPolicy;
import ru.runa.gpd.editor.gef.policy.TransitionConnectionEndpointsEditPolicy;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;

public class TransitionGraphicalEditPart extends AbstractConnectionEditPart implements PropertyNames, PropertyChangeListener, ActionsHost {
    @Override
    public Transition getModel() {
        return (Transition) super.getModel();
    }

    @Override
    public TransitionFigure getFigure() {
        return (TransitionFigure) super.getFigure();
    }

    @Override
    protected IFigure createFigure() {
        Transition transition = getModel();
        TransitionFigure figure = transition.getTypeDefinition().getGefEntry().createFigure(getModel().getProcessDefinition());
        figure.setRoutingConstraint(constructFigureBendpointList());
        figure.setLabelText(transition.getLabel());
        figure.addRoutingListener(new RoutingListener() {
            @Override
            public void invalidate(Connection connection) {
            }

            @Override
            public void postRoute(Connection connection) {
                if (!getModel().getProcessDefinition().isShowActions()) {
                    return;
                }
                getFigure().checkActionsFitInFigure();
            }

            @Override
            public void remove(Connection connection) {
            }

            @Override
            public boolean route(Connection connection) {
                return false;
            }

            @Override
            public void setConstraint(Connection connection, Object constraint) {
            }
        });
        // decorateFigure(figure);
        return figure;
    }

    private List<AbsoluteBendpoint> constructFigureBendpointList() {
        List<Point> modelBendpoints = getModel().getBendpoints();
        List<AbsoluteBendpoint> result = new ArrayList<AbsoluteBendpoint>(modelBendpoints.size());
        for (Point bendpoint : modelBendpoints) {
            result.add(new AbsoluteBendpoint(bendpoint));
        }
        return result;
    }

    @Override
    protected void refreshVisuals() {
        TransitionFigure f = getFigure();
        f.setLabelText(getModel().getLabel());
        f.setRoutingConstraint(constructFigureBendpointList());
    }

    @Override
    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new TransitionConnectionEndpointsEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ROLE, new TransitionConnectionEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new TransitionConnectionBendpointEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ActionContainerLayoutEditPolicy());
    }

    @Override
    public void activate() {
        if (!isActive()) {
            getModel().addPropertyChangeListener(this);
            if (getModel().getSource() instanceof ITimed) {
                getModel().getSource().addPropertyChangeListener(this);
            }
            if (getModel().getSource() instanceof Timer) {
                getModel().getSource().addPropertyChangeListener(this);
            }
            super.activate();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<? extends Object> getModelChildren() {
        if (getModel().getProcessDefinition().isShowActions()) {
            return getModel().getActions();
        }
        return super.getModelChildren();
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            getModel().removePropertyChangeListener(this);
            if (getModel().getSource() instanceof ITimed) {
                getModel().getSource().removePropertyChangeListener(this);
            }
            if (getModel().getSource() instanceof Timer) {
                getModel().getSource().removePropertyChangeListener(this);
            }
            super.deactivate();
        }
    }

    @Override
    public void refreshActionsVisibility(boolean visible) {
        refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (TRANSITION_BENDPOINTS_CHANGED.equals(messageId)) {
            refreshVisuals();
        } else if (PROPERTY_NAME.equals(messageId) && evt.getSource() instanceof Transition) {
            Transition transition = getModel();
            getFigure().setLabelText(transition.getLabel());
            refreshVisuals();
        } else if (PROPERTY_TIMER_DELAY.equals(messageId)) {
            Transition transition = getModel();
            if (transition.getName().equals(PluginConstants.TIMER_TRANSITION_NAME)) {
                Timer timer = null;
                if (transition.getSource() instanceof Timer) {
                    timer = (Timer) transition.getSource();
                }
                if (transition.getSource() instanceof ITimed) {
                    timer = ((ITimed) transition.getSource()).getTimer();
                }
                getFigure().setLabelText(timer != null ? timer.getDelay().toString() : "");
                refreshVisuals();
            }
        } else if (PROPERTY_CHILDS_CHANGED.equals(messageId)) {
            refreshChildren();
        }
    }
}
