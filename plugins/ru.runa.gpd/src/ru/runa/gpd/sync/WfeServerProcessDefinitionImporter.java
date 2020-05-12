package ru.runa.gpd.sync;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.gpd.PluginLogger;
import ru.runa.wfe.definition.DefinitionAlreadyExistException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionNameMismatchException;
import ru.runa.wfe.definition.dto.WfDefinition;

public class WfeServerProcessDefinitionImporter extends WfeServerConnectorDataImporter<List<WfDefinition>> {
    private static WfeServerProcessDefinitionImporter instance = new WfeServerProcessDefinitionImporter();

    public static WfeServerProcessDefinitionImporter getInstance() {
        return instance;
    }

    @Override
    protected List<WfDefinition> loadRemoteData() throws Exception {
        return getConnector().getProcessDefinitions();
    }

    public byte[] loadPar(WfDefinition definition) throws Exception {
        return getConnector().getProcessDefinitionArchive(definition);
    }

    // this method in data importer because it uses loaded data
    public void uploadPar(String definitionName, boolean updateLatestVersion, byte[] par, boolean retryWithSynchronize) {
        WfDefinition oldVersion = null;
        List<WfDefinition> data = getData();
        try {
            for (WfDefinition definition : data) {
                if (definitionName.equals(definition.getName())) {
                    oldVersion = definition;
                    break;
                }
            }
            if (updateLatestVersion && oldVersion != null) {
                getConnector().updateProcessDefinitionArchive(oldVersion.getId(), par);
            } else {
                if (oldVersion != null) {
                    String[] types = oldVersion.getCategories();
                    if (types == null) {
                        types = new String[] { "GPD" };
                    }
                    getConnector().redeployProcessDefinitionArchive(oldVersion.getId(), par, Lists.newArrayList(types));
                } else {
                    WfDefinition newDefinition = getConnector().deployProcessDefinitionArchive(par);
                    // changing underlying structure
                    data.add(newDefinition);
                }
            }
        } catch (Exception e) {
            if (retryWithSynchronize) {
                if (e instanceof DefinitionDoesNotExistException || e instanceof DefinitionAlreadyExistException
                        || e instanceof DefinitionNameMismatchException) {
                    PluginLogger.logInfo("Retrying due to " + e);
                    synchronize(null);
                    uploadPar(definitionName, updateLatestVersion, par, false);
                    return;
                }
            }
            Throwables.propagate(e);
        }
    }
}
