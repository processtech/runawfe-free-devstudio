package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.editor.gef.figure.ReceiveMessageFigure;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.jpdl.CatchEventNode;

public class ReceiveMessageGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_CHILDS_CHANGED);
    }

    @Override
    public CatchEventNode getModel() {
        return (CatchEventNode) super.getModel();
    }

    @Override
    public ReceiveMessageFigure getFigure() {
        return (ReceiveMessageFigure) super.getFigure();
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connectionEditPart) {
        String connectionName = ((Transition) connectionEditPart.getModel()).getName();
		if (PluginConstants.TIMER_TRANSITION_NAME.equals(connectionName)) {
            return getFigure().getTimerConnectionAnchor();
        } else if (PluginConstants.EVENT_TRANSITION_NAME.equals(connectionName)) { 
        	return getFigure().getEventConnectionAnchor();
        } else {
            return getFigure().getLeavingConnectionAnchor();
        }
    }

    @Override
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        if (getModel().getTimer() != null ) {
        	if ( getModel().getLeavingTransitions().size() == 1 && getModel().getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
        		return getFigure().getTimerConnectionAnchor();
        	}
        	if ( getModel().getLeavingTransitions().size() == 2 && getModel().getTransitionByName(PluginConstants.EVENT_TRANSITION_NAME) == null) {
        		return getFigure().getTimerConnectionAnchor();
        	}
        } 
        return getFigure().getLeavingConnectionAnchor();
    }
}
