package ru.runa.gpd.settings;

import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;

public class OrgFunctionPreferencePage extends ArtifactPreferencePage<OrgFunctionDefinition> {
    public OrgFunctionPreferencePage() {
        super(OrgFunctionsRegistry.getInstance());
    }
}
