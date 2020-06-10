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
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.var.file.FileVariable;

public class InputOutputComposite extends Composite {
    public final InputOutputModel model;
    private final Delegable delegable;
    private final String fileExtension;

    public InputOutputComposite(Composite parent, Delegable delegable, final InputOutputModel model, FilesSupplierMode mode, String fileExtension,
            DialogEnhancementMode dialogEnhancementMode) {
        super(parent, SWT.NONE);
        this.model = model;
        this.delegable = delegable;
        this.fileExtension = fileExtension;
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        setLayoutData(data);
        setLayout(new FillLayout(SWT.VERTICAL));
        if (mode.isInSupported()) {
            Group inputGroup = new Group(this, SWT.NONE);
            inputGroup.setText(Messages.getString("label.input"));
            inputGroup.setLayout(new GridLayout(2, false));

            new ChooseStringOrFile(inputGroup, model.inputPath, model.inputVariable, Messages.getString("label.filePath"), FilesSupplierMode.IN,
                    dialogEnhancementMode) {

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
            final Text fileNameText = new Text(outputGroup, SWT.BORDER);
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
            new ChooseStringOrFile(outputGroup, model.outputDir, model.outputVariable, Messages.getString("label.fileDir"), FilesSupplierMode.OUT,
                    dialogEnhancementMode) {
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

        public ChooseStringOrFile(Composite composite, String fileName, String variableName, String stringLabel, FilesSupplierMode mode,
                DialogEnhancementMode dialogEnhancementMode) {
            this.composite = composite;
            this.dialogEnhancementMode = dialogEnhancementMode;
            final boolean docxEnhancementModeInput = null != dialogEnhancementMode && dialogEnhancementMode.checkDocxEnhancementMode()
                    && dialogEnhancementMode.is(DocxDialogEnhancementMode.DOCX_SHOW_INPUT);

            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.add(stringLabel);
            combo.add(Messages.getString("label.fileVariable"));
            if (mode == FilesSupplierMode.IN) {
                combo.add(Messages.getString("label.processDefinitionFile"));
            }
            if (model.canWorkWithDataSource) {
                combo.add(Messages.getString("label.dataSourceName"));
                combo.add(Messages.getString("label.dataSourceNameVariable"));
            }

            if (!Strings.isNullOrEmpty(variableName)) {
                combo.select(1);
                showVariable(variableName);
            } else if ((delegable instanceof GraphElement && EmbeddedFileUtils.isProcessFile(fileName))
                    || (delegable instanceof BotTask && EmbeddedFileUtils.isBotTaskFile(fileName))) {
                combo.select(2);
                showEmbeddedFile(fileName, false);
            } else {
                if (docxEnhancementModeInput && (null == fileName) || (fileName.isEmpty())) {
                    combo.select(2);
                    showEmbeddedFile(fileName, true);
                } else {
                    combo.select(0);
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
                    switch (combo.getSelectionIndex()) {
                    case 0:
                        showFileName(null);
                        break;
                    case 2:
                        showEmbeddedFile(null, false);
                        break;
                    case 3:
                        showDataSource(null);
                        break;
                    case 4:
                        showDataSourceVariable(null);
                        break;
                    case 1:
                    default:
                        showVariable(null);
                    }
                }
            });
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
            }
            text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setFileName(text.getText(), false);
                }
            });
            control = text;
            composite.layout(true, true);
        }

        private void showVariable(String variable) {
            if (control != null) {
                control.dispose();
            }
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (String variableName : delegable.getVariableNames(false, FileVariable.class.getName())) {
                combo.add(variableName);
            }
            if (variable != null) {
                combo.setText(variable);
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
