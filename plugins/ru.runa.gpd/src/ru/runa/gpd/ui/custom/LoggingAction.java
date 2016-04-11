package ru.runa.gpd.ui.custom;

import org.eclipse.jface.action.Action;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingAction extends Action {
    public LoggingAction(String text) {
        super(text);
    }

    protected abstract void execute() throws Exception;

    @Override
    public void run() {
        try {
            execute();
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }
}
