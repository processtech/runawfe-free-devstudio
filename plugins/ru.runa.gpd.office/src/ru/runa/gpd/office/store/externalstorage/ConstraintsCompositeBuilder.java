package ru.runa.gpd.office.store.externalstorage;

public interface ConstraintsCompositeBuilder {
    void build();

    void onChangeVariableTypeName(String variableTypeName);

    void clearConstraints();
}