package ru.runa.gpd.office.excel;

import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;

public class SaveExcelHandlerCellEditorProvider extends BaseExcelHandlerCellEditorProvider {

    @Override
    protected String getTitle() {
        return Messages.getString("ExportExcelHandlerConfig.title");
    }

    @Override
    protected FilesSupplierMode getMode() {
        return FilesSupplierMode.BOTH;
    }

}
