package ru.runa.gpd.sync;

import java.util.List;
import ru.runa.wfe.bot.BotStation;

public class WfeServerBotStationImporter extends WfeServerConnectorDataImporter<List<BotStation>> {
    private static WfeServerBotStationImporter instance = new WfeServerBotStationImporter();

    public static WfeServerBotStationImporter getInstance() {
        return instance;
    }

    @Override
    protected List<BotStation> loadRemoteData() throws Exception {
        return WfeServerConnector.getInstance().getBotStations();
    }

    public byte[] getBotStationFile(BotStation botStation) throws Exception {
        return getConnector().getBotStationFile(botStation);
    }

}
