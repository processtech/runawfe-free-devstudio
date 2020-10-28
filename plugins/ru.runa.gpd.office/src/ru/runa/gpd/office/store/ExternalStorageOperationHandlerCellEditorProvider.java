package ru.runa.gpd.office.store;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.Set;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableUsedDataSourcesProvider;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;

public class ExternalStorageOperationHandlerCellEditorProvider extends BaseCommonStorageHandlerCellEditorProvider
        implements DelegableUsedDataSourcesProvider {
    public static final String DELIMITER = ":";
    public static final String DATASOURCE_PREFIX = "datasource";

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
            if (Strings.isNullOrEmpty(inputPath)) {
                return Collections.emptySet();
            }

            final String[] split = inputPath.split(DELIMITER);
            if (split.length == 2 && DATASOURCE_PREFIX.equals(split[0])) {
                return Collections.singleton(split[1]);
            }
            return Collections.emptySet();
        } catch (Exception e) {
            PluginLogger.logError(e);
            return Collections.emptySet();
        }
    }

}
