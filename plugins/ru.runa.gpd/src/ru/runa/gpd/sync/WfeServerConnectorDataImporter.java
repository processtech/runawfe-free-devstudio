package ru.runa.gpd.sync;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ui.custom.Dialogs;

public abstract class WfeServerConnectorDataImporter<T> {
    private static final String CACHE_FOLDER = "dataImporterCache";
    private WfeServerConnectorSettings settings;
    private T data;
    
    public final T getData() {
        if (data == null) {
            try {
                data = loadCachedData();
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("error.loadCachedData", e);
            }
        }
        if (data == null || !Objects.equals(settings, getConnector().getSettings())) {
            if (!getConnector().isConfigured()) {
                return null;
            }
            synchronize(null);
        }
        return data;
    }

    public final void synchronize(final WfeServerConnectorSynchronizationCallback callback) {
        Shell shell = Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : null;
        final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
        monitorDialog.setCancelable(true);
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    long start = System.currentTimeMillis();
                    monitor.beginTask(Localization.getString("task.SynchronizeData"), 120);
                    monitor.subTask(Localization.getString("task.Connect") + " " + getConnector().getSettings().getUrl());
                    getConnector().connect();
                    monitor.worked(10);
                    monitor.subTask(Localization.getString("task.LoadData"));
                    data = loadRemoteData();
                    settings = getConnector().getSettings();
                    monitor.subTask(Localization.getString("task.SaveData"));
                    saveCachedData(data);
                    monitor.done();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo(WfeServerConnectorDataImporter.this.getClass().getSimpleName() + " sync took " + (end - start) + " millis");
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
            if (callback != null) {
                callback.onCompleted();
            }
        } catch (InvocationTargetException ex) {
            String exceptionMessage = ex.getTargetException().getMessage();
            if (exceptionMessage != null
                    && exceptionMessage.contains("Unable to acquire version using")) {
                Dialogs.error(Localization.getString("error.synchronize.simulator.connect"), ex.getTargetException());
            } else if (exceptionMessage != null
                    && exceptionMessage.contains("Connection refused: connect")) {
                Dialogs.error(Localization.getString("error.synchronize.simulator.start"), ex.getTargetException());
            } else if (exceptionMessage != null
                    && exceptionMessage.contains("XML reader error")) {
                Dialogs.error(String.format(Localization.getString("error.synchronize.port"), WfeServerConnector.getInstance().getSettings().getPort()));
            } else {
                Dialogs.error(Localization.getString("error.Synchronize"), ex.getTargetException());
            }
            if (callback != null) {
                callback.onFailed();
            }
        } catch (InterruptedException consumed) {
        }
    }

    protected final WfeServerConnector getConnector() {
        return WfeServerConnector.getInstance();
    }

    protected abstract T loadRemoteData() throws Exception;

    protected T loadCachedData() throws Exception {
        return null;
    }

    protected void saveCachedData(T data) throws Exception {
    }

    protected final File getCacheFile() {
        File cacheFolder = new File(Activator.getPreferencesFolder() + File.separator + CACHE_FOLDER);
        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }
        return new File(cacheFolder, getClass().getSimpleName() + ".xml");
    }

}
