package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.editor.graphiti.TransitionUtil;
import ru.runa.gpd.lang.model.Decision;

public class ExclusiveGateway extends Decision {

    @Override
    public boolean isDelegable() {
        return isDecision();
    }

    public boolean isDecision() {
        return getLeavingTransitions().size() > 1;
    }

    @Override
    public void setDelegationConfiguration(String delegationConfiguration) {
        super.setDelegationConfiguration(delegationConfiguration);
        TransitionUtil.setDefaultFlow(this, delegationConfiguration);
    }

}
