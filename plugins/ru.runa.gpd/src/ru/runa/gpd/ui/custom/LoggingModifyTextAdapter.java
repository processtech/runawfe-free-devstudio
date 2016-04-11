package ru.runa.gpd.ui.custom;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingModifyTextAdapter implements ModifyListener {
    @Override
    public void modifyText(ModifyEvent e) {
        try {
            onTextChanged(e);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    protected abstract void onTextChanged(ModifyEvent e) throws Exception;
}
