package ru.runa.gpd.office.excel;

import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;

public class ReadExcelHandlerCellEditorProvider extends BaseExcelHandlerCellEditorProvider {

    @Override
    protected String getTitle() {
        return Messages.getString("ImportExcelHandlerConfig.title");
    }

    @Override
    protected FilesSupplierMode getMode() {
        return FilesSupplierMode.IN;
    }
}
