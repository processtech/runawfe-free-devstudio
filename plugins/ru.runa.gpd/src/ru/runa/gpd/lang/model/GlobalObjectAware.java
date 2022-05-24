package ru.runa.gpd.lang.model;

import org.eclipse.core.resources.IContainer;

public interface GlobalObjectAware {
    default void removeGlobalSwimlaneInAllProcesses(Swimlane swimlane, IContainer folder) {
    }

    default void removeGlobalVariableInAllProcesses(Variable variable, IContainer folder) {
    }

    default void removeGlobalVariableUserTypeInAllProcesses(VariableUserType type, IContainer folder) {
    }

    default void updateGlobalSwimlaneInAllProcesses(Swimlane swimlane, IContainer folder) {
    }

    default void updateGlobalVariableInAllProcesses(Variable variable, IContainer folder) {
    }

    default void updateGlobalVariableUserTypeInAllProcesses(VariableUserType type, IContainer folder) {
    }

}
