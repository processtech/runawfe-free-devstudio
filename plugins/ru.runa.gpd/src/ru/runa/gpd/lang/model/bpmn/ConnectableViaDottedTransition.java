package ru.runa.gpd.lang.model.bpmn;

import java.util.List;

public interface ConnectableViaDottedTransition {
    void addLeavingDottedTransition(DottedTransition transition);

    void removeLeavingDottedTransition(DottedTransition transition);

    void removeArrivingDottedTransition(DottedTransition transition);

    default boolean canAddArrivingDottedTransition(ConnectableViaDottedTransition source) {
        return getLeavingDottedTransitions().size() == 0 && getArrivingDottedTransitions().size() == 0 && !this.getClass().equals(source.getClass());
    }

    default boolean canAddLeavingDottedTransition() {
        return getLeavingDottedTransitions().size() == 0 && getArrivingDottedTransitions().size() == 0;
    }

    List<DottedTransition> getLeavingDottedTransitions();

    List<DottedTransition> getArrivingDottedTransitions();
}
