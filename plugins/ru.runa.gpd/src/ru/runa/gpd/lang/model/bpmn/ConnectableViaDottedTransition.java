package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

public interface ConnectableViaDottedTransition {
    void addLeavingDottedTransition(DottedTransition transition);

    void addArrivingDottedTransition(DottedTransition transition);

    void removeLeavingDottedTransition(DottedTransition transition);

    default void removeArrivingDottedTransition(DottedTransition transition) {
    }

    List<DottedTransition> getLeavingDottedTransitions();

    List<DottedTransition> getArrivingDottedTransitions();

    default boolean canAddArrivingDottedTransition(ConnectableViaDottedTransition source) {
        return getLeavingDottedTransitions().size() == 0 && getArrivingDottedTransitions().size() == 0 && !this.getClass().equals(source.getClass());
    }

    default boolean canAddLeavingDottedTransition() {
        return getLeavingDottedTransitions().size() == 0 && getArrivingDottedTransitions().size() == 0;
    }

    default boolean canReconnectLeavingDottedTransition(ConnectableViaDottedTransition target) {
        return !this.getClass().equals(target.getClass()) && canAddLeavingDottedTransition();
    }
}
