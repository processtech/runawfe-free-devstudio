package ru.runa.gpd.lang.model.jpdl;

import java.util.List;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.AbstractEventNode;
import ru.runa.gpd.lang.model.EventNodeType;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;

public class CatchEventNode extends AbstractEventNode implements ITimed {

    public CatchEventNode() {
        this.setEventNodeType(EventNodeType.message);
    }
    
    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && (transitions.size() == 0 || (transitions.size() == 1 && getTimer() != null));
    }

    @Override
    public Timer getTimer() {
        return getFirstChild(Timer.class);
    }

    @Override
    public String getNextTransitionName(NodeTypeDefinition typeDefinition) {
        if (getFirstChild(CatchEventNode.class) != null && getTransitionByName(PluginConstants.EVENT_TRANSITION_NAME) == null) {
            return PluginConstants.EVENT_TRANSITION_NAME;
        }
        return super.getNextTransitionName(typeDefinition);
    }

}
