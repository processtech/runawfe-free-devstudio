package ru.runa.gpd.office.store;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableUsedDataSourcesProvider;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.util.DataSourceUtils;

public class ExternalStorageOperationHandlerCellEditorProvider extends BaseCommonStorageHandlerCellEditorProvider
        implements DelegableUsedDataSourcesProvider {

    @Override
    protected FilesSupplierMode getMode() {
        return FilesSupplierMode.IN;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("ExternalStorageHandlerConfig.title");
    }

    @Override
    public Set<String> usedDataSourceNames(String configuration) {
        try {
            final String inputPath = fromXml(configuration).getInOutModel().inputPath;
            return Optional.ofNullable(DataSourceUtils.getDataSourceNameFromParameter(inputPath)).map(Collections::singleton)
                    .orElse(Collections.emptySet());
        } catch (Exception e) {
            PluginLogger.logError(e);
            return Collections.emptySet();
        }
    }

}
