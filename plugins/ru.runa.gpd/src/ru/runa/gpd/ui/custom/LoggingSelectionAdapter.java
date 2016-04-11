package ru.runa.gpd.ui.custom;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingSelectionAdapter extends SelectionAdapter {
    @Override
    public void widgetSelected(SelectionEvent event) {
        try {
            onSelection(event);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    protected abstract void onSelection(SelectionEvent e) throws Exception;
}
