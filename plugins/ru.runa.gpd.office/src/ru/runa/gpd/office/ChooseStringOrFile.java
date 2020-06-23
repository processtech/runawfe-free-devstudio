package ru.runa.gpd.office;

import com.google.common.base.Strings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.var.file.FileVariable;

class ChooseStringOrFile implements PropertyChangeListener {

    public void setFileName(String fileName, Boolean embeddedMode) {
        switch (this.mode) {
        case IN:
            model.inputPath = fileName;
            model.inputVariable = "";
            break;
        case OUT:
            model.outputDir = fileName;
            model.outputVariable = "";
            break;
        default:
        }
    }

    public void setVariable(String variable) {
        switch (this.mode) {
        case IN:
            model.inputPath = "";
            model.inputVariable = variable;
            break;
        case OUT:
            model.outputDir = "";
            model.outputVariable = variable;
            break;
        default:
        }
    }

    protected final Composite composite;
    private final InputOutputModel model;
    protected final Delegable delegable;
    private final String fileExtension;
    private final FilesSupplierMode mode;
    protected final DialogEnhancementMode dialogEnhancementMode;

    Control control = null;
    Combo variableCombo;

    // boolean docxEnhancementModeInput;
    // boolean docxEnhancementModeOutput;

    protected ChooseStringOrFile(Composite composite, InputOutputModel model, Delegable delegable, String fileExtension, FilesSupplierMode mode,
            DialogEnhancementMode dialogEnhancementMode) {
        this.composite = composite;
        this.model = model;
        this.delegable = delegable;
        this.fileExtension = fileExtension;
        this.mode = mode;
        this.dialogEnhancementMode = dialogEnhancementMode;
    }

    public ChooseStringOrFile(Composite composite, InputOutputModel model, Delegable delegable, String fileExtension, String stringLabel,
            FilesSupplierMode mode, DialogEnhancementMode dialogEnhancementMode) {

        this(composite, model, delegable, fileExtension, mode, dialogEnhancementMode);

        final String variableName = FilesSupplierMode.IN == mode ? model.inputVariable : model.outputVariable;
        final String fileName = FilesSupplierMode.IN == mode ? model.inputPath : model.outputDir;

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

    public boolean invokeEnhancementObserver(long flags) {
        if (null == dialogEnhancementMode) {
            return false;
        }
        variableCombo = null;
        boolean changed = false;

        if (DocxDialogEnhancementMode.check(flags, DocxDialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE)) {
            changed = 0 != model.inputVariable.compareTo(DocxDialogEnhancementMode.getInputFileParamName());
            showVariable(model.inputVariable = DocxDialogEnhancementMode.getInputFileParamName());
        } else {
            if (!changed) {
                changed = 0 != model.outputVariable.compareTo(DocxDialogEnhancementMode.getOutputFileParamName());
            }
            showVariable(model.outputVariable = DocxDialogEnhancementMode.getOutputFileParamName());
        }
        return changed;
    }

    protected void showFileName(String filename) {
        showFileName(filename, false, false, null);
    }

    protected void showFileName(String filename, boolean setEmptyFileName, boolean forceDispose, Boolean embeddedMode) {
        if (control != null) {
            if (forceDispose || !Text.class.isInstance(control)) {
                control.dispose();
            } else {
                return;
            }
        }
        final Text text = new Text(composite, SWT.BORDER);
        if (filename != null) {
            text.setText(filename);
        } else if (setEmptyFileName) {
            setFileName("", embeddedMode);
        }
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                setFileName(text.getText(), embeddedMode);
            }
        });
        control = text;
        composite.layout(true, true);
    }

    protected void showVariable(String variable) {
        if (control != null) {
            control.dispose();
        }
        final Combo combo = variableCombo = new Combo(composite, SWT.READ_ONLY);
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

    protected void showDataSource(String dataSource) {
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

    protected void showDataSourceVariable(String variable) {
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

    protected void showEmbeddedFile(String path, boolean showFileAsNewFirstTime) {
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

        if (dialogEnhancementMode != null && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
            IFile file = EmbeddedFileUtils.getProcessFile(fileName);
            boolean fileNotExists = null != file && !file.exists();
            updateEmbeddedFileName(fileNotExists || showFileAsNewFirstTime ? "" : fileName);
        } else {
            // http://sourceforge.net/p/runawfe/bugs/628/
            updateEmbeddedFileName(fileName);
        }

        if (null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
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
