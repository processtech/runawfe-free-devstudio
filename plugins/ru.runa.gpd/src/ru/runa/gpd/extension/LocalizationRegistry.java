package ru.runa.gpd.extension;

import java.io.File;
import java.util.List;

import ru.runa.gpd.Activator;
import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;

public class LocalizationRegistry extends ArtifactRegistry<Artifact> {
    private static final String XML_FILE_NAME = "localizations.xml";
    private static final LocalizationRegistry instance = new LocalizationRegistry();

    public LocalizationRegistry() {
        super(new ArtifactContentProvider<Artifact>());
    }

    public static LocalizationRegistry getInstance() {
        return instance;
    }

    @Override
    protected File getContentFile() {
        return new File(Activator.getPreferencesFolder(), XML_FILE_NAME);
    }

    @Override
    protected void loadDefaults(List<Artifact> list) {
        for (HandlerArtifact artifact : HandlerRegistry.getInstance().getAll()) {
            list.add(artifact);
        }
        for (OrgFunctionDefinition definition : OrgFunctionsRegistry.getInstance().getAll()) {
            list.add(definition);
        }
        for (VariableFormatArtifact artifact : VariableFormatRegistry.getInstance().getAll()) {
            list.add(artifact);
        }
    }

    public static String getLabel(String name) {
        Artifact artifact = getInstance().getArtifact(name);
        if (artifact != null) {
            return artifact.getLabel();
        }
        return name;
    }
}
