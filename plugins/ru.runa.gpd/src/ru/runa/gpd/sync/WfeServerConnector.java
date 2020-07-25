package ru.runa.gpd.sync;

import com.google.common.base.Throwables;
import java.util.List;
import java.util.Map;
import javax.security.auth.login.Configuration;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.definition.dto.WfDefinition;

public abstract class WfeServerConnector implements Connector, PrefConstants {
    private static WfeServerConnector instance = WfeServerConnectorRegistry.createConnector();
    protected WfeServerConnectorSettings settings = WfeServerConnectorSettings.loadSelected();

    static {
        Configuration.setConfiguration(new KerberosLoginConfiguration());
        System.setProperty("sun.net.client.defaultConnectTimeout", "15000");
        System.setProperty("sun.net.client.defaultReadTimeout", "15000");
    }

    public static WfeServerConnector getInstance() {
        return instance;
    }

    protected boolean isLoadProcessDefinitionsHistory() {
        return settings.isLoadProcessDefinitionsHistory();
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
        return settings.isConfigured();
    }

    public WfeServerConnectorSettings getSettings() {
        return settings;
    }

    public void setSettings(WfeServerConnectorSettings settings) {
        this.settings = settings;
    }

    /**
     * @return map of "Executor name" -> "Is group"
     */
    public abstract Map<String, Boolean> getExecutors();

    public abstract List<String> getRelationNames();

    public abstract List<WfDefinition> getProcessDefinitions();

    public abstract List<WfDefinition> getProcessDefinitionHistory(WfDefinition definition);

    public abstract byte[] getProcessDefinitionArchive(WfDefinition definition);

    public abstract WfDefinition deployProcessDefinitionArchive(byte[] par);

    public abstract WfDefinition redeployProcessDefinitionArchive(Long definitionId, byte[] par, List<String> types);

    public abstract WfDefinition updateProcessDefinitionArchive(Long definitionId, byte[] par);

    public abstract Map<Bot, List<BotTask>> getBots();

    public abstract byte[] getBotFile(Bot bot);

    public abstract byte[] getBotTaskFile(Bot bot, String botTask);

    public abstract void deployBot(String botStationName, byte[] archive);

    public abstract byte[] getBotStationFile(BotStation botStation);

    public abstract void deployBotStation(byte[] archive);

    public abstract List<BotStation> getBotStations();

    public abstract void deployDataSourceArchive(byte[] archive);

    public abstract byte[] getDataSourceArchive(String dsName);

    public abstract List<String> getDataSourceNames();

}
