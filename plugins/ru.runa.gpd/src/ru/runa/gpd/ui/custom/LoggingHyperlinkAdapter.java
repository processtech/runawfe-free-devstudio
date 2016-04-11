package ru.runa.gpd.ui.custom;

import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingHyperlinkAdapter extends HyperlinkAdapter {
    @Override
    public void linkActivated(HyperlinkEvent e) {
        try {
            onLinkActivated(e);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    protected abstract void onLinkActivated(HyperlinkEvent e) throws Exception;
}
