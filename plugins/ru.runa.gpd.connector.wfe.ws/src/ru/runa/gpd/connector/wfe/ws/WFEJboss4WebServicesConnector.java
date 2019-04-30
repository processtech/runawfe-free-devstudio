package ru.runa.gpd.connector.wfe.ws;

import java.net.MalformedURLException;
import java.net.URL;

import ru.runa.gpd.Activator;

public class WFEJboss4WebServicesConnector extends AbstractWebServicesConnector {
    @Override
    protected URL getUrl(String serviceName) {
        try {
            String version = getVersion();
            return new URL(getBaseUrl() + "/runawfe-wfe-service-" + version + "/" + serviceName + "ServiceBean?wsdl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
