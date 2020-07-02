package ru.runa.gpd.sync;

import java.util.List;

public class WfeServerDataSourceImporter extends WfeServerConnectorDataImporter<List<String>> {
    private static WfeServerDataSourceImporter instance = new WfeServerDataSourceImporter();

    public static WfeServerDataSourceImporter getInstance() {
        return instance;
    }

    @Override
    protected List<String> loadRemoteData() throws Exception {
        return getConnector().getDataSourceNames();
    }

    public byte[] getDataSourceFile(String name) {
        return getConnector().getDataSourceArchive(name);
    }

}
