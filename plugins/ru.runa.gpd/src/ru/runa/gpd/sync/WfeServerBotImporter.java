package ru.runa.gpd.sync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;

public class WfeServerBotImporter extends WfeServerConnectorDataImporter<Map<Bot, List<BotTask>>> {
    private static WfeServerBotImporter instance = new WfeServerBotImporter();

    public static WfeServerBotImporter getInstance() {
        return instance;
    }

    @Override
    protected Map<Bot, List<BotTask>> loadRemoteData() throws Exception {
        return getConnector().getBots();
    }

    public List<Bot> getBots() {
        List<Bot> result = new ArrayList<Bot>();
        if (getData() != null) {
            result.addAll(getData().keySet());
        }
        return result;
    }

    public List<BotTask> getBotTasks(Bot bot) {
        return getData().get(bot);
    }

    public byte[] getBotFile(Bot bot) throws Exception {
        return getConnector().getBotFile(bot);
    }

    public byte[] getBotTaskFile(Bot bot, String botTask) throws Exception {
        return getConnector().getBotTaskFile(bot, botTask);
    }

}
