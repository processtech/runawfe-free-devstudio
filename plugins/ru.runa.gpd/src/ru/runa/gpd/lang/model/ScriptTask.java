package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.extension.HandlerArtifact;

public class ScriptTask extends Node implements Delegable {

    @Override
    public String getDelegationType() {
        return HandlerArtifact.ACTION;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

}
