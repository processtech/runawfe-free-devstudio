package ru.runa.gpd.office;

import com.google.common.base.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.datasource.DataSourceStuff;

class ChooseStringOrFileScriptDocxTemplate extends ChooseStringOrFile {
    public ChooseStringOrFileScriptDocxTemplate(Composite composite, InputOutputModel model, Delegable delegable, String fileExtension,
            String stringLabel, FilesSupplierMode mode, DialogEnhancementMode dialogEnhancementMode) {

        super(composite, model, delegable, fileExtension, mode, dialogEnhancementMode);

        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        combo.add(Messages.getString(null != dialogEnhancementMode && dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_DONT_USE_VARIABLE_TITLE)
                ? "label.fileParameter"
                : "label.fileVariable"));
        if (mode == FilesSupplierMode.IN) {
            combo.add(Messages.getString("label.processDefinitionFileDocx"));
        }
        combo.add(stringLabel);
        if (model.canWorkWithDataSource) {
            combo.add(Messages.getString("label.dataSourceName"));
            combo.add(Messages.getString("label.dataSourceNameVariable"));
        }

        if (mode == FilesSupplierMode.IN) {
            String fileName = model.inputPath;
            String variableName = model.inputVariable;

            if (!Strings.isNullOrEmpty(variableName)) {
                combo.select(0);
                showVariable(variableName);
            } else if ((delegable instanceof GraphElement && (Strings.isNullOrEmpty(fileName) || EmbeddedFileUtils.isProcessFile(fileName)))
                    || (delegable instanceof BotTask && (Strings.isNullOrEmpty(fileName) || EmbeddedFileUtils.isBotTaskFile(fileName)))) {
                combo.select(1);
                showEmbeddedFile(fileName, false);
            } else {
                combo.select(2);
                showFileName(fileName);
                if (!Strings.isNullOrEmpty(fileName)) {
                    if (fileName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                        combo.select(3);
                        showDataSource(fileName);
                    } else if (fileName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
                        combo.select(4);
                        showDataSourceVariable(fileName);
                    }
                }
            }
        } else {
            String fileName = model.outputDir;
            if (!Strings.isNullOrEmpty(model.outputVariable) || Strings.isNullOrEmpty(fileName)) {
                combo.select(0);
                showVariable(model.outputVariable);
            } else {
                combo.select(1);
                showFileName(fileName);
                if (!Strings.isNullOrEmpty(fileName)) {
                    if (fileName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                        combo.select(2);
                        showDataSource(fileName);
                    } else if (fileName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
                        combo.select(3);
                        showDataSourceVariable(fileName);
                    }
                }
            }
        }

        combo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                variableCombo = null;
                if (mode == FilesSupplierMode.IN) {
                    switch (combo.getSelectionIndex()) {
                    case 0:
                        showVariable(model.inputVariable);
                        break;
                    case 1:
                        showEmbeddedFile(null, false);
                        break;
                    case 2:
                        showFileName(null);
                        break;
                    case 3:
                        showDataSource(null);
                        break;
                    case 4:
                        showDataSourceVariable(null);
                        break;
                    default:
                    }
                } else {
                    switch (combo.getSelectionIndex()) {
                    case 0:
                        showVariable(model.outputVariable);
                        break;
                    case 1:
                        showFileName(null);
                        break;
                    case 2:
                        showDataSource(null);
                        break;
                    case 3:
                        showDataSourceVariable(null);
                        break;
                    default:
                    }
                }

            }
        });
    }
}
