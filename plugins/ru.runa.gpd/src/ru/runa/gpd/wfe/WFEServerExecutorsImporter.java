package ru.runa.gpd.wfe;

import org.eclipse.core.runtime.IProgressMonitor;

public class WFEServerExecutorsImporter extends ExecutorsImporter {
    private static WFEServerExecutorsImporter instance;

    @Override
    protected WFEServerConnector getConnector() {
        return WFEServerConnector.getInstance();
    }

    public static synchronized WFEServerExecutorsImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerExecutorsImporter();
        }
        return instance;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        executors.putAll(WFEServerConnector.getInstance().getExecutors());
        monitor.worked(100);
    }
}
