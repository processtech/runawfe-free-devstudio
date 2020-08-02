package ru.runa.gpd.sync;

import java.util.Map;

public class WfeServerExecutorImporter extends AbstractConnectorExecutorImporter {
    private static WfeServerExecutorImporter instance = new WfeServerExecutorImporter();

    public static WfeServerExecutorImporter getInstance() {
        return instance;
    }

    @Override
    protected Map<String, Boolean> loadRemoteData() throws Exception {
        return WfeServerConnector.getInstance().getExecutors();
    }
}
