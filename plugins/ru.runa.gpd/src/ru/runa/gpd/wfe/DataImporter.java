package ru.runa.gpd.wfe;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;

public abstract class DataImporter {
    protected abstract IConnector getConnector();

    public boolean isConfigured() {
        return getConnector().isConfigured();
    }

    public void connect() throws Exception {
        getConnector().connect();
    }

    protected File getCacheFile() {
        String fileName = getClass().getSimpleName() + ".xml";
        return new File(Activator.getPreferencesFolder(), fileName);
    }

    protected abstract void clearInMemoryCache();

    protected abstract void loadRemoteData(IProgressMonitor monitor) throws Exception;

    protected abstract void saveCachedData() throws Exception;

    public abstract Object loadCachedData() throws Exception;

    public boolean hasCachedData() {
        try {
            return loadCachedData() != null;
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("", e);
            return false;
        }
    }

    public final void synchronize() {
        Shell shell = Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : null;
        final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
        monitorDialog.setCancelable(true);
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    monitor.beginTask(Localization.getString("task.SynchronizeData"), 120);
                    monitor.subTask(Localization.getString("task.Connect"));
                    connect();
                    monitor.worked(10);
                    monitor.subTask(Localization.getString("task.LoadData"));
                    clearInMemoryCache();
                    loadRemoteData(monitor);
                    // monitor.worked(1);
                    monitor.subTask(Localization.getString("task.SaveData"));
                    saveCachedData();
                    monitor.done();
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("error.Synchronize", e);
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            monitorDialog.run(true, false, runnable);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex.getTargetException());
        } catch (InterruptedException ex) {
            // 
        }
    }
}
