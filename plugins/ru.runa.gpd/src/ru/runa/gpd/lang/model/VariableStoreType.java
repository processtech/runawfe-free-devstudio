package ru.runa.gpd.lang.model;

import ru.runa.gpd.Localization;

/**
 * @author Alekseev Vitaly
 * @since Jun 21, 2017
 */
public enum VariableStoreType {
    DEFAULT(Localization.getString("VariableStoreType.default.description")),
    BLOB(Localization.getString("VariableStoreType.blob.description"));

    private final String description;

    private VariableStoreType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String asProperty() {
        return name().toLowerCase();
    }
}
