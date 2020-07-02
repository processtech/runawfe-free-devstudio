package ru.runa.gpd.sync;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;

public class WfeServerConnectorRegistry implements PrefConstants {
    private static IConfigurationElement connectorConfigurationElement;
    static {
        try {
            IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.wfeServerConnectors").getExtensions();
            for (IExtension extension : extensions) {
                IConfigurationElement[] configElements = extension.getConfigurationElements();
                for (IConfigurationElement configElement : configElements) {
                    if (connectorConfigurationElement != null) {
                        throw new RuntimeException("Only single connector supported now");
                    }
                    connectorConfigurationElement = configElement;
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("Error processing extension 'ru.runa.gpd.wfeServerConnectors'", e);
        }
    }

    public static WfeServerConnector createConnector() {
        try {
            if (connectorConfigurationElement == null) {
                return new WfeServerConnectorStub();
            }
            return (WfeServerConnector) connectorConfigurationElement.createExecutableExtension("class");
        } catch (Exception e) {
            throw new RuntimeException("Error processing extension 'ru.runa.gpd.wfeServerConnectors'", e);
        }
    }

}
