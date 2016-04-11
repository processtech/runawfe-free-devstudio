package ru.runa.gpd.settings;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;

public class HandlerPreferencePage extends ArtifactPreferencePage<HandlerArtifact> {
    public HandlerPreferencePage() {
        super(HandlerRegistry.getInstance());
    }
}
