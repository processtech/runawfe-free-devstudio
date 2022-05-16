package ru.runa.gpd.connector.wfe.ws;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.bot.BotStation;

public class BotStationAdapter {

    public static BotStation toDTO(ru.runa.wfe.webservice.BotStation botStation) {
        if (botStation == null) {
            return null;
        }
        BotStation result = new BotStation();
        result.setId(botStation.getId());
        result.setCreateDate(DateAdapter.toDTO(botStation.getCreateDate()));
        result.setVersion(botStation.getVersion());
        result.setName(botStation.getName());
        return result;
    }

    public static List<BotStation> toDTOs(List<ru.runa.wfe.webservice.BotStation> botStations) {
        List<BotStation> result = Lists.newArrayListWithExpectedSize(botStations.size());
        for (ru.runa.wfe.webservice.BotStation bot : botStations) {
            result.add(toDTO(bot));
        }
        return result;
    }

    public static ru.runa.wfe.webservice.BotStation toJAXB(BotStation botStation) {
        ru.runa.wfe.webservice.BotStation result = new ru.runa.wfe.webservice.BotStation();
        result.setId(botStation.getId());
        result.setCreateDate(DateAdapter.toJAXB(botStation.getCreateDate()));
        result.setVersion(botStation.getVersion());
        result.setName(botStation.getName());
        return result;
    }
}
