package ru.runa.gpd.connector.wfe.ws;

import java.util.List;

import ru.runa.wfe.bot.Bot;

import com.google.common.collect.Lists;

public class BotAdapter {

    public static Bot toDTO(ru.runa.wfe.webservice.Bot bot) {
        Bot result = new Bot();
        result.setId(bot.getId());
        result.setCreateDate(DateAdapter.toDTO(bot.getCreateDate()));
        result.setPassword(bot.getPassword());
        result.setUsername(bot.getUsername());
        result.setVersion(bot.getVersion());
        return result;
    }

    public static List<Bot> toDTOs(List<ru.runa.wfe.webservice.Bot> bots) {
        List<Bot> result = Lists.newArrayListWithExpectedSize(bots.size());
        for (ru.runa.wfe.webservice.Bot bot : bots) {
            result.add(toDTO(bot));
        }
        return result;
    }

    public static ru.runa.wfe.webservice.Bot toJAXB(Bot bot) {
        ru.runa.wfe.webservice.Bot result = new ru.runa.wfe.webservice.Bot();
        result.setId(bot.getId());
        result.setCreateDate(DateAdapter.toJAXB(bot.getCreateDate()));
        result.setPassword(bot.getPassword());
        result.setUsername(bot.getUsername());
        result.setVersion(bot.getVersion());
        return result;
    }
}
