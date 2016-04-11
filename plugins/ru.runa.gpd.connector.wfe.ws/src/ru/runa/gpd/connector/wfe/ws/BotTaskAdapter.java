package ru.runa.gpd.connector.wfe.ws;

import java.util.List;

import ru.runa.wfe.bot.BotTask;

import com.google.common.collect.Lists;

public class BotTaskAdapter {

    public static BotTask toDTO(ru.runa.wfe.webservice.BotTask botTask) {
        BotTask result = new BotTask();
        result.setId(botTask.getId());
        result.setCreateDate(DateAdapter.toDTO(botTask.getCreateDate()));
        result.setConfiguration(botTask.getConfiguration());
        result.setName(botTask.getName());
        result.setTaskHandlerClassName(botTask.getTaskHandlerClassName());
        result.setVersion(botTask.getVersion());
        return result;
    }

    public static List<BotTask> toDTOs(List<ru.runa.wfe.webservice.BotTask> botTasks) {
        List<BotTask> result = Lists.newArrayListWithExpectedSize(botTasks.size());
        for (ru.runa.wfe.webservice.BotTask bot : botTasks) {
            result.add(toDTO(bot));
        }
        return result;
    }
}
