package ru.runa.gpd.lang.model.jpdl;

import java.util.List;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.IReceiveMessageNode;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;

public class ReceiveMessageNode extends MessageNode implements IReceiveMessageNode {

    public ReceiveMessageNode() {
        super();
        nodeRegulationsProperties.setIsEnabled(false);
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
        if (getTimer() != null && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName(typeDefinition);
    }

}
