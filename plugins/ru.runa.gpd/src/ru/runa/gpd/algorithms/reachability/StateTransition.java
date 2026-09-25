package ru.runa.gpd.algorithms.reachability;

import java.util.Collections;
import java.util.List;

public class StateTransition {
    private final TokenState source;
    private final TokenState target;
    private final List<ProcessElement> firedElements;

    StateTransition(TokenState source, TokenState target, List<ProcessElement> firedElements) {
        this.source = source;
        this.target = target;
        this.firedElements = Collections.unmodifiableList(firedElements);
    }

    public TokenState getSource() {
        return source;
    }

    public TokenState getTarget() {
        return target;
    }

    public List<ProcessElement> getFiredElements() {
        return firedElements;
    }
}
