package ru.runa.gpd.lang.model.jpdl;

import java.util.List;

import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class Join extends Node {
    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }
}
