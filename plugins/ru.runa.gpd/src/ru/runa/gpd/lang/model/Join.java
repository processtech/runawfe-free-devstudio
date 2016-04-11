package ru.runa.gpd.lang.model;

import java.util.List;

public class Join extends Node {
    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }
}
