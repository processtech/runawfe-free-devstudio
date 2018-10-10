package ru.runa.gpd.wfe;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.google.common.collect.Lists;

public class WFEServerDataSourceImporter extends DataImporter {
    
    private final List<String> dataSourceNames = Lists.newArrayList();
    private static WFEServerDataSourceImporter instance;

    @Override
    protected WFEServerConnector getConnector() {
        return WFEServerConnector.getInstance();
    }

    public static synchronized WFEServerDataSourceImporter getInstance() {
        if (instance == null) {
            instance = new WFEServerDataSourceImporter();
        }
        return instance;
    }

    @Override
    public boolean hasCachedData() {
        return dataSourceNames.size() > 0;
    }

    @Override
    protected void clearInMemoryCache() {
        dataSourceNames.clear();
    }

    @Override
    public Object loadCachedData() throws Exception {
        return dataSourceNames;
    }

    @Override
    protected void loadRemoteData(IProgressMonitor monitor) throws Exception {
        dataSourceNames.addAll(getConnector().getDataSourceNames());
    }

    @Override
    protected void saveCachedData() throws Exception {
    }

    public byte[] getDataSourceFile(String name) {
        return getConnector().getDataSourceArchive(name);
    }

    public void deployDataSource(byte[] archive) {
        getConnector().deployDataSourceArchive(archive);
    }

    public List<String> getDataSourceNames() {
        return getConnector().getDataSourceNames();
    }

}
