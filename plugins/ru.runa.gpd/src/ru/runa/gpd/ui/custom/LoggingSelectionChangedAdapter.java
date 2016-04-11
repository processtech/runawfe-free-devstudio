package ru.runa.gpd.ui.custom;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingSelectionChangedAdapter implements ISelectionChangedListener {
    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        try {
            onSelectionChanged(event);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    protected abstract void onSelectionChanged(SelectionChangedEvent event) throws Exception;
}
