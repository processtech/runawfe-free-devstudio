package ru.runa.gpd.lang.model;

public class ExclusiveGateway extends Decision {
    @Override
    public boolean isDelegable() {
        return isDecision();
    }

    public boolean isDecision() {
        return getLeavingTransitions().size() > 1;
    }
}
