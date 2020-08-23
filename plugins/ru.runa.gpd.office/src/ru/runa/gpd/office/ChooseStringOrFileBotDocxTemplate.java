package ru.runa.gpd.office;

import com.google.common.base.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.var.file.FileVariable;

class ChooseStringOrFileBotDocxTemplate extends ChooseStringOrFile {

    final boolean docxEnhancementModeInput;

    public ChooseStringOrFileBotDocxTemplate(Composite composite, InputOutputModel model, Delegable delegable, String fileExtension,
            String stringLabel, FilesSupplierMode mode, DialogEnhancementMode dialogEnhancementMode) {

        super(composite, model, delegable, fileExtension, mode, dialogEnhancementMode);
        docxEnhancementModeInput = mode == FilesSupplierMode.IN;

        final Combo combo = new Combo(composite, SWT.READ_ONLY);
        if (docxEnhancementModeInput) {
            combo.add(Messages.getString("label.processDefinitionFile"));
        }
        combo.add(Messages.getString(null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()
                && dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_DONT_USE_VARIABLE_TITLE) ? "label.fileParameter" : "label.fileVariable"));
        combo.add(stringLabel);
        if (model.canWorkWithDataSource) {
            combo.add(Messages.getString("label.dataSourceName"));
            combo.add(Messages.getString("label.dataSourceNameVariable"));
        }

        if (docxEnhancementModeInput) {
            String fileName = model.inputPath;
            String variableName = model.inputVariable;

            if (!Strings.isNullOrEmpty(variableName)) {
                combo.select(1);
                showVariable(variableName);
            } else if ((delegable instanceof GraphElement && (Strings.isNullOrEmpty(fileName) || EmbeddedFileUtils.isProcessFile(fileName)))
                    || (delegable instanceof BotTask && (Strings.isNullOrEmpty(fileName) || EmbeddedFileUtils.isBotTaskFile(fileName)))) {
                combo.select(0);
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
            String variableName = model.outputVariable;

            if (!Strings.isNullOrEmpty(variableName) || Strings.isNullOrEmpty(fileName)) {
                combo.select(0);
                showVariable(variableName);
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
                if (docxEnhancementModeInput) {
                    switch (combo.getSelectionIndex()) {
                    case 0:
                        deleteInputVariable();
                        showEmbeddedFile(null, false);
                        break;
                    case 1:
                        showVariable(model.inputVariable);
                        break;
                    case 2:
                        deleteInputVariable();
                        showFileName(null);
                        break;
                    case 3:
                        deleteInputVariable();
                        showDataSource(null);
                        break;
                    case 4:
                        deleteInputVariable();
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
                        deleteOutputVariable();
                        showFileName(null);
                        break;
                    case 2:
                        deleteOutputVariable();
                        showDataSource(null);
                        break;
                    case 3:
                        deleteOutputVariable();
                        showDataSourceVariable(null);
                        break;
                    default:
                    }
                }
            }
        });
    }

    private void createInputVariable() {
        dialogEnhancementMode.invoke(DialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE | DialogEnhancementMode.DOCX_CREATE_VARIABLE);
    }

    private void createOutputVariable() {
        dialogEnhancementMode.invoke(DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE | DialogEnhancementMode.DOCX_CREATE_VARIABLE);
    }

    private void deleteInputVariable() {
        dialogEnhancementMode.invoke(DialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE | DialogEnhancementMode.DOCX_DELETE_VARIABLE);
    }

    private void deleteOutputVariable() {
        dialogEnhancementMode.invoke(DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE | DialogEnhancementMode.DOCX_DELETE_VARIABLE);
    }

    @Override
    protected void showFileName(String filename) {
        showFileName(filename, true, true, docxEnhancementModeInput ? false : null);
    }

    @Override
    protected void showVariable(String variable) {

        if (control != null) {
            control.dispose();
        }
        final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setBackground(new Color(null, 229, 229, 229));

        boolean ok = false;

        Delegable delegable = null;

        if (null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
            DocxDialogEnhancementMode docxDialogEnhancementMode = (DocxDialogEnhancementMode) dialogEnhancementMode;
            delegable = docxDialogEnhancementMode.getBotTask();
        }

        boolean needInvokeParent = false;
        if (!Strings.isNullOrEmpty(variable)) {
            for (String variableName : delegable.getVariableNames(false, FileVariable.class.getName())) {
                if (variable.compareTo(variableName) == 0) {
                    ok = true;
                }
            }
            if (ok) {
                text.setText(variable);
                setVariable(variable);
            }
        }
        if (!ok) {
            setVariable("");
            needInvokeParent = true;
        }

        control = text;
        composite.layout(true, true);

        if (needInvokeParent) {
            if (docxEnhancementModeInput) {
                createInputVariable();
            } else {
                createOutputVariable();
            }
        }

    }

}
