package ru.runa.gpd.extension.bot;

import ru.runa.gpd.lang.model.BotTask;

public interface IBotFileSupportProvider {
	String getEmbeddedFileName(BotTask botTask);
	void taskRenamed(BotTask botTask, String oldName, String newName);
}
