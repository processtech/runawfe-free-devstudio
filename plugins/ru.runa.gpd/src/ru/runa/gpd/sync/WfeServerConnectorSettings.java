package ru.runa.gpd.sync;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.settings.WfeServerConnectorPreferenceNode;
import ru.runa.gpd.settings.WfeServerConnectorsPreferenceNode;
import ru.runa.gpd.ui.dialog.UserInputDialog;

public class WfeServerConnectorSettings implements PrefConstants {
    private static final String PROTOCOL_SPLITTER = "://";
    private final String protocol;
    private final String host;
    private final int port;
    private String version;
    private final String authenticationType;
    private final String login;
    private final String password;
    private String userInputPassword;
    private final boolean loadProcessDefinitionsHistory;
    private final boolean allowUpdateLastVersionByKeyBinding;

    public static WfeServerConnectorSettings load(int index) {
        return new WfeServerConnectorSettings(index);
    }

    public static WfeServerConnectorSettings loadSelected() {
        return new WfeServerConnectorSettings(WfeServerConnectorsPreferenceNode.getSelectedIndex());
    }

    public static WfeServerConnectorSettings createDefault() {
        return new WfeServerConnectorSettings();
    }

    private WfeServerConnectorSettings(int index) {
        String prefix = WfeServerConnectorPreferenceNode.getId(index);
        this.protocol = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX);
        String hostSetting = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX);
        String[] hostProtocol = hostSetting.split(PROTOCOL_SPLITTER);
        if (hostProtocol.length == 2) {
            this.host = hostProtocol[1];
        } else {
            this.host = hostSetting;
        }
        String portString = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_PORT_SUFFIX);
        int portNumber = 0;
        try {
            portNumber = Integer.parseInt(portString);
        } catch (NumberFormatException e) {
        }
        this.port = portNumber;
        this.authenticationType = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX);
        this.login = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX);
        this.password = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX);
        this.loadProcessDefinitionsHistory = Activator.getPrefBoolean(prefix + '.' + P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX);
        this.allowUpdateLastVersionByKeyBinding = Activator
                .getPrefBoolean(prefix + '.' + P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX);
    }

    private WfeServerConnectorSettings() {
        this.protocol = "http";
        this.host = "localhost";
        this.port = 8080;
        this.authenticationType = AUTHENTICATION_TYPE_LOGIN_PASSWORD;
        this.login = "Administrator";
        this.password = "wf";
        this.loadProcessDefinitionsHistory = false;
        this.allowUpdateLastVersionByKeyBinding = false;
    }

    public boolean isConfigured() {
        if (protocol.length() == 0) {
            return false;
        }
        if (host.length() == 0) {
            return false;
        }
        if (port == 0) {
            return false;
        }
        if (AUTHENTICATION_TYPE_LOGIN_PASSWORD.equals(authenticationType)) {
            if (login.length() == 0) {
                return false;
            }
        }
        return true;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        if (!password.isEmpty()) {
            return password;
        }
        if (userInputPassword == null) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    UserInputDialog userInputDialog = new UserInputDialog(Localization.getString("pref.connection.password"));
                    if (Window.OK == userInputDialog.open()) {
                        userInputPassword = userInputDialog.getUserInput();
                    }
                }
            });
            if (userInputPassword.length() == 0) {
                PluginLogger.logInfo("[wfeserverconnector] empty password");
                return null;
            }
        }
        return userInputPassword;
    }

    public boolean isLoadProcessDefinitionsHistory() {
        return loadProcessDefinitionsHistory;
    }

    public boolean isAllowUpdateLastVersionByKeyBinding() {
        return allowUpdateLastVersionByKeyBinding;
    }

    public String getUrl() {
        return protocol + PROTOCOL_SPLITTER + host + ":" + port;
    }

}
