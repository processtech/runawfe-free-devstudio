package ru.runa.gpd.settings;

import ru.runa.gpd.extension.VariableFormatArtifact;
import ru.runa.gpd.extension.VariableFormatRegistry;

public class VariableFormatPreferencePage extends ArtifactPreferencePage<VariableFormatArtifact> {
    public VariableFormatPreferencePage() {
        super(VariableFormatRegistry.getInstance());
    }
}
