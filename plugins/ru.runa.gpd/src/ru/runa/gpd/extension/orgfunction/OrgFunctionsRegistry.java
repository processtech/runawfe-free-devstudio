package ru.runa.gpd.extension.orgfunction;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.ArtifactContentProvider;
import ru.runa.gpd.extension.ArtifactRegistry;
import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.collect.Lists;

public class OrgFunctionsRegistry extends ArtifactRegistry<OrgFunctionDefinition> {
    private static final OrgFunctionsRegistry instance = new OrgFunctionsRegistry();

    public static OrgFunctionsRegistry getInstance() {
        return instance;
    }

    @Override
    protected File getContentFile() {
        return null;
    }

    public OrgFunctionsRegistry() {
        super(new ArtifactContentProvider<OrgFunctionDefinition>());
    }

    @Override
    protected void loadDefaults(List<OrgFunctionDefinition> list) {
        list.add(OrgFunctionDefinition.DEFAULT);
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.orgFunctions").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    String className = configElement.getAttribute("className");
                    String label = configElement.getAttribute("label");
                    boolean canBeUsedForEscalation = TypeConversionUtil.convertTo(boolean.class, configElement.getAttribute("canBeUsedForEscalation"));
                    List<OrgFunctionParameterDefinition> parameters = Lists.newArrayList();
                    IConfigurationElement[] parameterElements = configElement.getChildren();
                    for (IConfigurationElement paramElement : parameterElements) {
                        OrgFunctionParameterDefinition parameterDefinition = new OrgFunctionParameterDefinition(paramElement.getAttribute("name"),
                                paramElement.getAttribute("type"), Boolean.valueOf(paramElement.getAttribute("multiple")));
                        parameters.add(parameterDefinition);
                    }
                    OrgFunctionDefinition orgFunctionDefinition = new OrgFunctionDefinition(className, label, parameters, canBeUsedForEscalation);
                    orgFunctionDefinition.checkMultipleParameters();
                    list.add(orgFunctionDefinition);
                } catch (Exception e) {
                    PluginLogger.logError("Error processing 'orgFunctions' element", e);
                }
            }
        }
    }
}
