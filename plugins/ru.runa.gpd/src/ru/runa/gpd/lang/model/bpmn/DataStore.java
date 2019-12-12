package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class DataStore extends Node implements IBoundaryEventContainer {
    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return super.allowLeavingTransition(transitions) && transitions.size() == 0;
    }

}
