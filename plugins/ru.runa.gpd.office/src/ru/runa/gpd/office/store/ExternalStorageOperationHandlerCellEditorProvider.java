package ru.runa.gpd.office.store;

import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;

public class ExternalStorageOperationHandlerCellEditorProvider extends BaseCommonStorageHandlerCellEditorProvider {

    @Override
    protected FilesSupplierMode getMode() {
        return FilesSupplierMode.IN;
    }

    @Override
    protected String getTitle() {
        return Messages.getString("ExternalStorageHandlerConfig.title");
    }

}
