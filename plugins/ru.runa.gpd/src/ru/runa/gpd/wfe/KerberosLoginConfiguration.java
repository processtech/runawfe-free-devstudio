package ru.runa.gpd.wfe;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

public class KerberosLoginConfiguration extends Configuration {
    static {
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String appName) {
        if (appName.equals("com.sun.security.jgss.initiate")) {
            AppConfigurationEntry appConfigurationEntry = new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, getInitParameters());
            return new AppConfigurationEntry[] { appConfigurationEntry };
        }
        return null;
    }

    private Map<String, String> getInitParameters() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("useTicketCache", "true");
        params.put("doNotPrompt", "true");
        params.put("debug", "true");
        return params;
    }

    @Override
    public void refresh() {
    }
}
