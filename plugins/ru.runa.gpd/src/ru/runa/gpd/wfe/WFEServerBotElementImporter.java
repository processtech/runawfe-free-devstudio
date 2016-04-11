package ru.runa.gpd.wfe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;

public class WFEServerBotElementImporter extends DataImporter {
    private final Map<Bot, List<BotTask>> bots = new HashMap<Bot, List<BotTask>>();
    private static WFEServerBotElementImporter instance;

    @Override
    protected WFEServerConnector getConnector() {
        return WFEServerConnector.getInstance();
    }

    public static synchronized WFEServerBotElementImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerBotElementImporter();
        }
        return instance;
    }

    @Override
    public boolean hasCachedData() {
        return bots.size() > 0;
    }

    @Override
    protected void clearInMemoryCache() {
        bots.clear();
    }

    @Override
    public Object loadCachedData() throws Exception {
        return bots;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        bots.putAll(getConnector().getBots());
    }

    @Override
    protected void saveCachedData() throws Exception {
    }

    public List<Bot> getBots() {
        List<Bot> result = new ArrayList<Bot>();
        if (bots.keySet() != null && bots.keySet().size() > 0) {
            result.addAll(bots.keySet());
        }
        return result;
    }

    public List<BotTask> getBotTasks() {
        List<BotTask> result = new ArrayList<BotTask>();
        for (List<BotTask> botTaskArray : bots.values()) {
            result.addAll(botTaskArray);
        }
        return result;
    }

    public List<BotTask> getBotTasks(Bot bot) {
        return bots.get(bot);
    }

    public byte[] getBotFile(Bot bot) throws Exception {
        return getConnector().getBotFile(bot);
    }

    public byte[] getBotTaskFile(Bot bot, String botTask) throws Exception {
        return getConnector().getBotTaskFile(bot, botTask);
    }

    public void deployBot(String botStationName, byte[] archive) {
        try {
            getConnector().deployBot(botStationName, archive);
        } catch (BotStationDoesNotExistException e) {
            Dialogs.error(Localization.getString("ExportBotWizardPage.page.notExistWarning"));
        }
    }
}
