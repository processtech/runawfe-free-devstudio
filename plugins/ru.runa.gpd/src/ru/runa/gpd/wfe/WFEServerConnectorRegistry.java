package ru.runa.gpd.wfe;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;

public class WFEServerConnectorRegistry implements PrefConstants {
    private static IConfigurationElement connectorConfigurationElement;
    static {
        try {
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.wfeConnectors").getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    if (connectorConfigurationElement != null) {
                        throw new RuntimeException("Now only single connector supported");
                    }
                    connectorConfigurationElement = configElement;
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Error processing extension 'ru.runa.gpd.wfeConnectors'", e);
        }
    }

    public static WFEServerConnector createConnector() {
        try {
            if (connectorConfigurationElement == null) {
                return new WFEServerConnectorStub();
            }
            return (WFEServerConnector) connectorConfigurationElement.createExecutableExtension("class");
        } catch (Exception e) {
            throw new RuntimeException("Error processing extension 'ru.runa.gpd.wfeConnectors'", e);
        }
    }

}
