package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;

public class ReceiveMessageNode extends MessagingNode implements ITimed {

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
        if (getProcessDefinition().getLanguage() == Language.JPDL && getTimer() != null && getTransitionByName(PluginConstants.TIMER_TRANSITION_NAME) == null) {
            return PluginConstants.TIMER_TRANSITION_NAME;
        }
        return super.getNextTransitionName(typeDefinition);
    }
    
}
