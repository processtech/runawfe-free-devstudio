package ru.runa.gpd.extension.bot;

import ru.runa.gpd.lang.model.BotTask;

/**
 * Bot handler task which can contain file
 */
public interface IBotFileSupportProvider {

    /**
     * @return embedded file name or <code>null</code>
     */
    String getEmbeddedFileName(BotTask botTask);

    void taskRenamed(BotTask botTask, String oldName, String newName);

}
