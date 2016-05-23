package ru.runa.gpd.wfe;

import java.util.List;
import java.util.Map;

import javax.security.auth.login.Configuration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.dialog.UserInputDialog;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.base.Throwables;

public abstract class WFEServerConnector implements IConnector, PrefConstants {
    static {
        Configuration.setConfiguration(new KerberosLoginConfiguration());
    }
    private static WFEServerConnector instance;
    private String userInputPassword;

    public static synchronized WFEServerConnector getInstance() {
        if (instance == null) {
            instance = WFEServerConnectorRegistry.createConnector();
        }
        return instance;
    }

    public static synchronized void destroy() {
        instance = null;
    }

    protected String getPassword() {
        String password = Activator.getPrefString(P_WFE_CONNECTION_PASSWORD);
        if (password.length() == 0) {
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
                password = userInputPassword;
            }
            if (password.length() == 0) {
                PluginLogger.logInfo("[wfeconnector] empty password");
                return null;
            }
        }
        return password;
    }

    protected boolean isLoadProcessDefinitionsHistory() {
        return Activator.getPrefBoolean(P_WFE_LOAD_PROCESS_DEFINITIONS_HISTORY);
    }

    protected byte[] getKerberosToken() {
        try {
            GSSManager manager = GSSManager.getInstance();
            GSSCredential clientCred = manager.createCredential(GSSCredential.INITIATE_ONLY);
            GSSName peerName = manager.createName("WFServer", null);
            GSSContext context = manager.createContext(peerName, (Oid) null, clientCred, GSSContext.DEFAULT_LIFETIME);
            context.requestMutualAuth(false);
            byte[] token = new byte[0];
            token = context.initSecContext(token, 0, token.length);
            return token;
        } catch (GSSException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public boolean isConfigured() {
        if (Activator.getPrefString(P_WFE_CONNECTION_HOST).length() == 0) {
            return false;
        }
        if (Activator.getPrefString(P_WFE_CONNECTION_PORT).length() == 0) {
            return false;
        }
        if (LOGIN_MODE_LOGIN_PASSWORD.equals(Activator.getPrefString(P_WFE_CONNECTION_LOGIN_MODE))) {
            if (Activator.getPrefString(P_WFE_CONNECTION_LOGIN).length() == 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Map<Executor name, Is group>
     */
    public abstract Map<String, Boolean> getExecutors();

    public abstract List<String> getRelationNames();

    public abstract Map<WfDefinition, List<WfDefinition>> getProcessDefinitions(IProgressMonitor monitor);

    public abstract byte[] getProcessDefinitionArchive(WfDefinition definition);

    public abstract WfDefinition deployProcessDefinitionArchive(byte[] par);

    public abstract WfDefinition redeployProcessDefinitionArchive(Long definitionId, byte[] par, List<String> types);

    public abstract Map<Bot, List<BotTask>> getBots();

    public abstract byte[] getBotFile(Bot bot);

    public abstract byte[] getBotTaskFile(Bot bot, String botTask);

    public abstract void deployBot(String botStationName, byte[] archive);

    public abstract byte[] getBotStationFile(BotStation botStation);

    public abstract void deployBotStation(byte[] archive);

    public abstract List<BotStation> getBotStations();

    public abstract void setSetting(String properties, String name, String value);
}
