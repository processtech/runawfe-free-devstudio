package ru.runa.gpd.office;

import com.google.common.base.Strings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.office.word.DocxModel;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DialogEnhancementObserver;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;

public class InputOutputComposite extends Composite implements DialogEnhancementObserver {
    public final InputOutputModel model;
    private final DialogEnhancementMode dialogEnhancementMode;
    private ChooseStringOrFile chooseStringOrFileOutput, chooseStringOrFileInput;
    private Text fileNameText;

    public InputOutputComposite(Composite parent, Delegable delegable, final InputOutputModel model, FilesSupplierMode mode, String fileExtension,
            DialogEnhancementMode dialogEnhancementMode) {
        super(parent, SWT.NONE);
        this.model = model;
        this.dialogEnhancementMode = dialogEnhancementMode;
        if (null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
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

            if (null != dialogEnhancementMode) {
                if (dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
                    chooseStringOrFileInput = new ChooseStringOrFileBotDocxTemplate(inputGroup, model, delegable, fileExtension,
                            Messages.getString("label.filePath"), FilesSupplierMode.IN, dialogEnhancementMode) {
                        @Override
                        public void setFileName(String fileName, Boolean embeddedMode) {
                            super.setFileName(fileName, embeddedMode);
                            boolean enableReadDocxButton = EmbeddedFileUtils.isBotTaskFile(fileName);
                            updateDialogEnhancementMode(dialogEnhancementMode, fileName, enableReadDocxButton, embeddedMode);
                        }

                        @Override
                        public void setVariable(String variable) {
                            super.setVariable(variable);
                            updateDialogEnhancementMode(dialogEnhancementMode, "", false, false);
                        }
                    };
                } else if (dialogEnhancementMode.checkScriptDocxTemplateEnhancementMode()) {
                    chooseStringOrFileInput = new ChooseStringOrFileScriptDocxTemplate(inputGroup, model, delegable, fileExtension,
                            Messages.getString("label.filePath"), FilesSupplierMode.IN, dialogEnhancementMode);
                }
            }
            if (null == chooseStringOrFileInput) {
                chooseStringOrFileInput = new ChooseStringOrFile(inputGroup, model, delegable, fileExtension, Messages.getString("label.filePath"),
                        FilesSupplierMode.IN, dialogEnhancementMode);
            }

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
            if (null != dialogEnhancementMode) {
                if (dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
                    chooseStringOrFileOutput = new ChooseStringOrFileBotDocxTemplate(outputGroup, model, delegable, fileExtension,
                            Messages.getString("label.filePath"), FilesSupplierMode.OUT, dialogEnhancementMode) {
                        @Override
                        public void setFileName(String fileName, Boolean embeddedMode) {
                            super.setFileName(fileName, embeddedMode);
                            updateDialogEnhancementMode(dialogEnhancementMode, null, null, embeddedMode);
                        }

                        @Override
                        public void setVariable(String variable) {
                            super.setVariable(variable);
                            updateDialogEnhancementMode(dialogEnhancementMode, null, null, null);
                        }
                    };
                } else if (dialogEnhancementMode.checkScriptDocxTemplateEnhancementMode()) {
                    chooseStringOrFileOutput = new ChooseStringOrFileScriptDocxTemplate(outputGroup, model, delegable, fileExtension,
                            Messages.getString("label.filePath"), FilesSupplierMode.OUT, dialogEnhancementMode) {
                        @Override
                        public void setVariable(String variable) {
                            super.setVariable(variable);
                            if (!Strings.isNullOrEmpty(variable) && Strings.isNullOrEmpty(model.outputFilename)) {
                                fileNameText.setText(model.outputFilename = variable + ".docx");
                            }
                        }
                    };
                }
            }
            if (null == chooseStringOrFileOutput) {
                chooseStringOrFileOutput = new ChooseStringOrFile(outputGroup, model, delegable, fileExtension, Messages.getString("label.filePath"),
                        FilesSupplierMode.OUT, dialogEnhancementMode);
            }
        }
        layout(true, true);
    }

    @Override
    public void invokeEnhancementObserver(long flags) {

        if (null == dialogEnhancementMode || !dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
            return;
        }

        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE)) {
            String outputFileParamName = DocxDialogEnhancementMode.getOutputFileParamName();
            if (null != fileNameText && fileNameText.getText().isEmpty()) {
                fileNameText.setText(model.outputFilename = outputFileParamName + ".docx");
            }
        }
        boolean changedIn = false;
        boolean changedOut = false;
        if (null != chooseStringOrFileInput && DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE)) {
            changedIn = chooseStringOrFileInput.invokeEnhancementObserver(flags);
        }
        if (null != chooseStringOrFileOutput && DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE)) {
            changedOut = chooseStringOrFileOutput.invokeEnhancementObserver(flags);
        }
        if (changedIn || changedOut) {
            updateDialogEnhancementMode(dialogEnhancementMode, null, null, null);
        }
    }

    private void updateDialogEnhancementMode(DialogEnhancementMode dialogEnhancementMode, String embeddedFileName, Boolean enableReadDocxButton,
            Boolean enableDocxMode) {

        if (null == dialogEnhancementMode || !dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
            return;
        }

        DocxModel docxModel = (DocxModel) ((DocxDialogEnhancementMode) dialogEnhancementMode).docxModel;
        ((DocxDialogEnhancementMode) dialogEnhancementMode).reloadXmlFromModel(docxModel.toString(), embeddedFileName, enableReadDocxButton,
                enableDocxMode);

    }

}
