package ru.runa.gpd.office;

import com.google.common.base.Strings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.word.DocxModel;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DialogEnhancementObserver;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.var.file.FileVariable;

public class InputOutputComposite extends Composite implements DialogEnhancementObserver {
    public final InputOutputModel model;
    private final Delegable delegable;
    private final String fileExtension;
    private final DialogEnhancementMode dialogEnhancementMode;
    private ChooseStringOrFile chooseStringOrFileOutput;
    private Text fileNameText;

    public InputOutputComposite(Composite parent, Delegable delegable, final InputOutputModel model, FilesSupplierMode mode, String fileExtension,
            DialogEnhancementMode dialogEnhancementMode) {
        super(parent, SWT.NONE);
        this.model = model;
        this.delegable = delegable;
        this.fileExtension = fileExtension;
        this.dialogEnhancementMode = dialogEnhancementMode;
        if (null != dialogEnhancementMode && dialogEnhancementMode.checkDocxEnhancementMode()) {
            ((DocxDialogEnhancementMode) dialogEnhancementMode).observer = this;
        }
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        setLayoutData(data);
        setLayout(new FillLayout(SWT.VERTICAL));
        if (mode.isInSupported()) {
            Group inputGroup = new Group(this, SWT.NONE);
            inputGroup.setText(Messages.getString("label.input"));
            inputGroup.setLayout(new GridLayout(2, false));

            chooseStringOrFileOutput = new ChooseStringOrFile(inputGroup, model.inputPath, model.inputVariable, Messages.getString("label.filePath"),
                    FilesSupplierMode.IN, dialogEnhancementMode) {

                @Override
                public void setFileName(String fileName, Boolean embeddedMode) {
                    model.inputPath = fileName;
                    model.inputVariable = "";

                    boolean enableReadDocxButton = EmbeddedFileUtils.isBotTaskFile(fileName);
                    updateDialogEnhancementMode(dialogEnhancementMode, fileName, enableReadDocxButton, embeddedMode);
                }

                @Override
                public void setVariable(String variable) {
                    model.inputPath = "";
                    model.inputVariable = variable;
                    updateDialogEnhancementMode(dialogEnhancementMode, "", false, false);
                }
            };

        }
        if (mode.isOutSupported()) {
            Group outputGroup = new Group(this, SWT.NONE);
            outputGroup.setText(Messages.getString("label.output"));
            outputGroup.setLayout(new GridLayout(2, false));
            Label fileNameLabel = new Label(outputGroup, SWT.NONE);
            fileNameLabel.setText(Messages.getString("label.fileName"));
            fileNameText = new Text(outputGroup, SWT.BORDER);
            fileNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (model.outputFilename != null) {
                fileNameText.setText(model.outputFilename);
            }
            fileNameText.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    model.outputFilename = fileNameText.getText();
                    updateDialogEnhancementMode(dialogEnhancementMode, null, null, null);
                }
            });
            chooseStringOrFileOutput = new ChooseStringOrFile(outputGroup, model.outputDir, model.outputVariable, Messages.getString("label.fileDir"),
                    FilesSupplierMode.OUT, dialogEnhancementMode) {
                @Override
                public void setFileName(String fileName, Boolean embeddedMode) {
                    model.outputDir = fileName;
                    model.outputVariable = "";
                    updateDialogEnhancementMode(dialogEnhancementMode, null, null, embeddedMode);
                }

                @Override
                public void setVariable(String variable) {
                    model.outputDir = "";
                    model.outputVariable = variable;
                    updateDialogEnhancementMode(dialogEnhancementMode, null, null, null);
                }
            };
        }
        layout(true, true);
    }

    @Override
    public void invokeEnhancementObserver(long flags) {
        if (null != dialogEnhancementMode && dialogEnhancementMode.checkDocxEnhancementMode()
                && DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE_SELECTED)) {
            String outputFileParamName = DocxDialogEnhancementMode.getOutputFileParamName();
            if (null != fileNameText && fileNameText.getText().isEmpty()) {
                fileNameText.setText(model.outputFilename = outputFileParamName + ".docx");
            }
        }
        if (null != chooseStringOrFileOutput) {
            chooseStringOrFileOutput.invokeEnhancementObserver(flags);
        }
    }

    private void updateDialogEnhancementMode(DialogEnhancementMode dialogEnhancementMode, String embeddedFileName, Boolean enableReadDocxButton,
            Boolean enableDocxMode) {
        if (null != dialogEnhancementMode && dialogEnhancementMode.checkDocxEnhancementMode()) {
            DocxModel docxModel = (DocxModel) ((DocxDialogEnhancementMode) dialogEnhancementMode).docxModel;
            ((DocxDialogEnhancementMode) dialogEnhancementMode).reloadXmlFromModel(docxModel.toString(), embeddedFileName, enableReadDocxButton,
                    enableDocxMode);
        }
    }

    private abstract class ChooseStringOrFile implements PropertyChangeListener {
        public abstract void setFileName(String fileName, Boolean embeddedMode);

        public abstract void setVariable(String variable);

        private Control control = null;
        private final Composite composite;
        private final DialogEnhancementMode dialogEnhancementMode;
        final boolean docxEnhancementModeInput;
        final boolean docxEnhancementModeOutput;
        final boolean docxScriptEnhancementMode;
        private Combo variableCombo;

        public ChooseStringOrFile(Composite composite, String fileName, String variableName, String stringLabel, FilesSupplierMode mode,
                DialogEnhancementMode dialogEnhancementMode) {
            this.composite = composite;
            if (null != (this.dialogEnhancementMode = dialogEnhancementMode) && dialogEnhancementMode.checkDocxEnhancementMode()) {
                docxEnhancementModeInput = dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_SHOW_INPUT);
                docxEnhancementModeOutput = dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_SHOW_OUTPUT);
            } else {
                docxEnhancementModeInput = false;
                docxEnhancementModeOutput = false;
            }
            docxScriptEnhancementMode = null != dialogEnhancementMode && dialogEnhancementMode.checkScriptDocxEnhancementMode();
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            if (!docxScriptEnhancementMode) {
                combo.add(stringLabel);
            }
            combo.add(Messages.getString(null != dialogEnhancementMode && dialogEnhancementMode.checkDocxEnhancementMode()
                    && dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_DONT_USE_VARIABLE_TITLE) ? "label.fileParameter"
                            : "label.fileVariable"));
            if (mode == FilesSupplierMode.IN) {
                combo.add(Messages.getString(docxScriptEnhancementMode ? "label.processDefinitionFileDocx" : "label.processDefinitionFile"));
            }
            if (model.canWorkWithDataSource) {
                combo.add(Messages.getString("label.dataSourceName"));
                combo.add(Messages.getString("label.dataSourceNameVariable"));
            }
            if (docxScriptEnhancementMode) {
                combo.add(stringLabel);
            }

            if (!Strings.isNullOrEmpty(variableName)) {
                combo.select(docxScriptEnhancementMode ? 0 : 1);
                showVariable(variableName);
            } else if ((delegable instanceof GraphElement && EmbeddedFileUtils.isProcessFile(fileName))
                    || (delegable instanceof BotTask && EmbeddedFileUtils.isBotTaskFile(fileName))) {
                combo.select(docxScriptEnhancementMode ? 1 : 2);
                showEmbeddedFile(fileName, false);
            } else {
                // default behavior
                if (docxEnhancementModeInput && Strings.isNullOrEmpty(fileName)) {
                    combo.select(2);
                    showEmbeddedFile(fileName, true);
                } else if (docxEnhancementModeOutput && Strings.isNullOrEmpty(variableName)) {
                    combo.select(1);
                    showVariable(variableName);
                } else if (docxScriptEnhancementMode) {
                    if (mode == FilesSupplierMode.IN) {
                        combo.select(1);
                        showEmbeddedFile(fileName, true);
                    } else {
                        combo.select(0);
                        showVariable(variableName);
                    }
                } else {
                    combo.select(docxScriptEnhancementMode ? 2 : 0);
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
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    variableCombo = null;
                    switch (combo.getSelectionIndex()) {
                    case 0:
                        if (docxScriptEnhancementMode) {
                            showVariable(null);
                        } else {
                            showFileName(null);
                        }
                        break;
                    case 2:
                        if (docxScriptEnhancementMode) {
                            showFileName(null);
                        } else {
                            showEmbeddedFile(null, false);
                        }
                        break;
                    case 3:
                        if (!docxScriptEnhancementMode) {
                            showDataSource(null);
                        }
                        break;
                    case 4:
                        if (!docxScriptEnhancementMode) {
                            showDataSourceVariable(null);
                        }
                        break;
                    case 1:
                        if (docxScriptEnhancementMode) {
                            if (mode == FilesSupplierMode.IN) {
                                showEmbeddedFile(null, false);
                            } else {
                                showVariable(null);
                            }
                            break;
                        }
                    default:
                        if (!docxScriptEnhancementMode) {
                            showVariable(null);
                        }
                    }
                }
            });
        }

        public void invokeEnhancementObserver(long flags) {
            if (null == variableCombo || null == dialogEnhancementMode) {
                return;
            }
            variableCombo = null;
            showVariable(DocxDialogEnhancementMode.check(flags, DocxDialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE_SELECTED)
                    ? DocxDialogEnhancementMode.getInputFileParamName()
                    : DocxDialogEnhancementMode.getOutputFileParamName());
        }

        private void showFileName(String filename) {
            if (control != null) {
                if (!Text.class.isInstance(control)) {
                    control.dispose();
                } else {
                    return;
                }
            }
            final Text text = new Text(composite, SWT.BORDER);
            if (filename != null) {
                text.setText(filename);
            } else if (docxEnhancementModeInput || docxEnhancementModeOutput) {
                setFileName("", docxEnhancementModeInput ? false : null);
            }
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setFileName(text.getText(), docxEnhancementModeInput ? false : null);
                }
            });
            control = text;
            composite.layout(true, true);
        }

        private void showVariable(String variable) {
            if (control != null) {
                control.dispose();
            }
            final Combo combo = variableCombo = new Combo(composite, SWT.READ_ONLY);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (String variableName : delegable.getVariableNames(false, FileVariable.class.getName())) {
                combo.add(variableName);
            }
            boolean needInvokeParent = false;
            if (variable != null) {
                combo.setText(variable);
            } else if (docxEnhancementModeInput || docxEnhancementModeOutput) {
                setVariable("");
                needInvokeParent = true;
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    setVariable(combo.getText());
                }
            });
            combo.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setVariable(combo.getText());
                }
            });
            control = combo;
            composite.layout(true, true);

            if (needInvokeParent) {
                dialogEnhancementMode.invoke(docxEnhancementModeInput ? DialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE_SELECTED
                        : DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE_SELECTED);
            }
        }

        private void showDataSource(String dataSource) {
            if (control != null) {
                control.dispose();
            }
            final Combo combo = new Combo(composite, SWT.NONE);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (IFile dsFile : DataSourceUtils.getAllDataSources()) {
                String dsName = dsFile.getName();
                combo.add(dsName.substring(0, dsName.length() - DataSourceStuff.DATA_SOURCE_FILE_SUFFIX.length()));
            }
            if (dataSource != null) {
                combo.setText(dataSource.substring(dataSource.indexOf(':') + 1));
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    setFileName(DataSourceStuff.PATH_PREFIX_DATA_SOURCE + combo.getText(), null);
                }
            });
            combo.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setFileName(DataSourceStuff.PATH_PREFIX_DATA_SOURCE + combo.getText(), null);
                }
            });
            control = combo;
            composite.layout(true, true);
        }

        private void showDataSourceVariable(String variable) {
            if (control != null) {
                control.dispose();
            }
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (String variableName : delegable.getVariableNames(false, String.class.getName())) {
                combo.add(variableName);
            }
            if (variable != null) {
                combo.setText(variable.substring(variable.indexOf(':') + 1));
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    setFileName(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE + combo.getText(), null);
                }
            });
            combo.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setFileName(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE + combo.getText(), null);
                }
            });
            control = combo;
            composite.layout(true, true);
        }

        private void showEmbeddedFile(String path, boolean showFileAsNewFirstTime) {
            if (control != null) {
                if (TemplateFileComposite.class != control.getClass()) {
                    control.dispose();
                } else {
                    return;
                }
            }

            String fileName;

            if (delegable instanceof GraphElement) {
                if (EmbeddedFileUtils.isProcessFile(path)) {
                    fileName = EmbeddedFileUtils.getProcessFileName(path);
                } else {
                    fileName = EmbeddedFileUtils.generateEmbeddedFileName(delegable, fileExtension);
                }
            } else if (delegable instanceof BotTask) {
                if (EmbeddedFileUtils.isBotTaskFile(path)) {
                    fileName = EmbeddedFileUtils.getBotTaskFileName(path);
                } else {
                    fileName = EmbeddedFileUtils.generateEmbeddedFileName(delegable, fileExtension);
                }
            } else {
                throw new InternalApplicationException("Unexpected classtype " + delegable);
            }

            if (dialogEnhancementMode != null && dialogEnhancementMode.checkDocxEnhancementMode()) {
                IFile file = EmbeddedFileUtils.getProcessFile(fileName);
                boolean fileNotExists = null != file && !file.exists();
                updateEmbeddedFileName(fileNotExists || showFileAsNewFirstTime ? "" : fileName);
            } else {
                // http://sourceforge.net/p/runawfe/bugs/628/
                updateEmbeddedFileName(fileName);
            }

            if (null != dialogEnhancementMode && dialogEnhancementMode.checkDocxEnhancementMode()) {
                ((DocxDialogEnhancementMode) dialogEnhancementMode).defaultFileName = fileName;
                ((DocxDialogEnhancementMode) dialogEnhancementMode).showFileAsNewFirstTime = showFileAsNewFirstTime;
            }

            control = new TemplateFileComposite(composite, fileName, fileExtension, dialogEnhancementMode);
            ((TemplateFileComposite) control).getEventSupport().addPropertyChangeListener(this);
            composite.layout(true, true);

        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            updateEmbeddedFileName((String) event.getNewValue());
        }

        private void updateEmbeddedFileName(String fileName) {
            if (delegable instanceof GraphElement) {
                setFileName(EmbeddedFileUtils.getProcessFilePath(fileName), null);
            } else if (delegable instanceof BotTask) {
                setFileName(EmbeddedFileUtils.getBotTaskFilePath(fileName), true);
            } else {
                throw new InternalApplicationException("Unexpected classtype " + delegable);
            }
        }

    }

}
