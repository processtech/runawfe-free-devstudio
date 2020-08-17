package ru.runa.gpd.connector.wfe.ws;

import java.net.MalformedURLException;
import java.net.URL;

public class WildflyWebServiceWfeServerConnector extends AbstractWebServiceWfeServerConnector {

    @Override
    protected URL getUrl(String serviceName) {
        try {
            String version = getVersion();
            return new URL(settings.getUrl() + "/wfe-service-" + version + "/" + serviceName + "WebService/" + serviceName + "API?wsdl");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
