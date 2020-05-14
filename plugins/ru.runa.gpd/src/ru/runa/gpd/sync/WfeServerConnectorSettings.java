package ru.runa.gpd.sync;

import org.eclipse.jface.preference.IPreferenceStore;
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
    private final int index;
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
        return new WfeServerConnectorSettings(index, true);
    }

    public static WfeServerConnectorSettings loadSelected() {
        return new WfeServerConnectorSettings(WfeServerConnectorsPreferenceNode.getSelectedIndex(), true);
    }

    public static WfeServerConnectorSettings createDefault(int index) {
        return new WfeServerConnectorSettings(index, false);
    }

    private WfeServerConnectorSettings(int index, boolean loadFromStore) {
        this.index = index;
        if (loadFromStore) {
            String prefix = WfeServerConnectorPreferenceNode.getId(index);
            this.protocol = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX);
            this.host = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX);
            this.port = Activator.getPrefInt(prefix + '.' + P_WFE_SERVER_CONNECTOR_PORT_SUFFIX);
            this.authenticationType = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX);
            this.login = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX);
            this.password = Activator.getPrefString(prefix + '.' + P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX);
            this.loadProcessDefinitionsHistory = Activator
                    .getPrefBoolean(prefix + '.' + P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX);
            this.allowUpdateLastVersionByKeyBinding = Activator
                    .getPrefBoolean(prefix + '.' + P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX);
        } else {
            this.protocol = "http";
            this.host = "localhost";
            this.port = 8080;
            this.authenticationType = AUTHENTICATION_TYPE_LOGIN_PASSWORD;
            this.login = "Administrator";
            this.password = "wf";
            this.loadProcessDefinitionsHistory = false;
            this.allowUpdateLastVersionByKeyBinding = false;
        }
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
        return protocol + "://" + host + ":" + port;
    }

    public void saveToStore() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String prefix = WfeServerConnectorPreferenceNode.getId(index);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX, protocol);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX, host);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_PORT_SUFFIX, port);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX, authenticationType);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX, login);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX, password);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX, loadProcessDefinitionsHistory);
        store.setValue(prefix + "." + P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX, allowUpdateLastVersionByKeyBinding);
    }

    public void saveDefaultToStore() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String prefix = WfeServerConnectorPreferenceNode.getId(index);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX, protocol);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX, host);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_PORT_SUFFIX, port);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX, authenticationType);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX, login);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX, password);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX, loadProcessDefinitionsHistory);
        store.setDefault(prefix + "." + P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX, allowUpdateLastVersionByKeyBinding);
    }

    public void removeFromStore() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        String prefix = WfeServerConnectorPreferenceNode.getId(index);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_PROTOCOL_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_HOST_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_PORT_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_AUTHENTICATION_TYPE_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_LOGIN_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_PASSWORD_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_LOAD_PROCESS_DEFINITIONS_HISTORY_SUFFIX);
        store.setToDefault(prefix + '.' + P_WFE_SERVER_CONNECTOR_ALLOW_UPDATE_LAST_VERSION_BY_KEY_BINDING_SUFFIX);
    }
}
