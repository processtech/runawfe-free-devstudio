package ru.runa.gpd.sync;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.dialog.UserInputDialog;

public class LdapServerConnector implements Connector, PrefConstants {
    private static LdapServerConnector instance = new LdapServerConnector();
    private DirContext dirContext;

    public static LdapServerConnector getInstance() {
        return instance;
    }

    private LdapServerConnector() {
    }

    public DirContext getDirContext() {
        return dirContext;
    }

    public String getName() {
        return Activator.getPrefString(P_LDAP_CONNECTION_PROVIDER_URL);
    }

    @Override
    public void connect() throws Exception {
        String providerUrl = Activator.getPrefString(P_LDAP_CONNECTION_PROVIDER_URL);
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, providerUrl);
        if (AUTHENTICATION_TYPE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_LDAP_CONNECTION_LOGIN_MODE))) {
            String login = Activator.getPrefString(P_LDAP_CONNECTION_LOGIN);
            String password = Activator.getPrefString(P_LDAP_CONNECTION_PASSWORD);
            if (password.length() == 0) {
                UserInputDialog userInputDialog = new UserInputDialog(Localization.getString("pref.connection.password"));
                if (Window.OK == userInputDialog.open()) {
                    password = userInputDialog.getUserInput();
                }
                if (password.length() == 0) {
                    PluginLogger.logInfo("[ldapconnector] empty password");
                    return;
                }
            }
            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, login);
            env.put(Context.SECURITY_CREDENTIALS, password);
        } else {
            env.put(Context.SECURITY_AUTHENTICATION, "GSSAPI");
        }
        env.put("java.naming.ldap.version", "3");
        this.dirContext = new InitialDirContext(env);
    }

    @Override
    public boolean isConfigured() {
        if (Activator.getPrefString(P_LDAP_CONNECTION_PROVIDER_URL).length() == 0) {
            return false;
        }
        if (AUTHENTICATION_TYPE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_LDAP_CONNECTION_LOGIN_MODE))) {
            if (Activator.getPrefString(P_LDAP_CONNECTION_LOGIN).length() == 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void disconnect() throws Exception {
        dirContext.close();
    }
}
