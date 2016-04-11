package ru.runa.gpd.settings;

import ru.runa.gpd.extension.Artifact;
import ru.runa.gpd.extension.LocalizationRegistry;

public class LocalizationPreferencePage extends ArtifactPreferencePage<Artifact> {
    public LocalizationPreferencePage() {
        super(LocalizationRegistry.getInstance());
    }
}
