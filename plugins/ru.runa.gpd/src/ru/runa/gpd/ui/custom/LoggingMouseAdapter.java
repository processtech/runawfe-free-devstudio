package ru.runa.gpd.ui.custom;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

import ru.runa.gpd.PluginLogger;

public abstract class LoggingMouseAdapter implements MouseListener {
    @Override
    public void mouseDoubleClick(MouseEvent e) {
        try {
            onMouseDoubleClick(e);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    @Override
    public void mouseDown(MouseEvent e) {
        try {
            onMouseDown(e);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    @Override
    public void mouseUp(MouseEvent e) {
        try {
            onMouseUp(e);
        } catch (Throwable th) {
            PluginLogger.logError(th);
        }
    }

    protected abstract void onMouseDoubleClick(MouseEvent e) throws Exception;

    protected abstract void onMouseDown(MouseEvent e) throws Exception;

    protected abstract void onMouseUp(MouseEvent e) throws Exception;

}
