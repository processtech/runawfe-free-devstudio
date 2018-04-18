package ru.runa.gpd.office.word;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.TemplateFileComposite;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.file.FileVariable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class MergeInputOutputComposite extends Composite {
    public final MergeInputOutputModel model;
    private final Delegable delegable;
    private final String fileExtension;
    private final int size = 1;
    private Group inputGroup;
    private Group outputGroup;
    private Hyperlink hyperLink;
    private final List<Integer> typeInputList = Lists.newArrayList();
    private Integer outputType = null;

    public MergeInputOutputComposite(Composite parent, Delegable delegable, final MergeInputOutputModel model, String fileExtension) {
        super(parent, SWT.BORDER);
        this.model = model;
        this.delegable = delegable;
        this.fileExtension = fileExtension;
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        setLayoutData(data);
        setLayout(new FillLayout(SWT.VERTICAL));
        updateUI();
    }

    private void updateUI() {
        if (inputGroup != null) {
            inputGroup.dispose();
        }
        if (outputGroup != null) {
            outputGroup.dispose();
        }
        if (hyperLink != null) {
            hyperLink.dispose();
        }
        initInGroup();
        addMultiInControls(model);
        hyperLink = initAddLink(model);
        addOutControls(model);
        getParent().layout(true, true);
    }

    private Hyperlink initAddLink(final MergeInputOutputModel model) {
        return SWTUtils.createLink(inputGroup, Localization.getString("button.create"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                model.getInputPathList().add(null);
                model.getInputVariableList().add(null);
                typeInputList.add(0);
                updateUI();
            }
        });
    }

    private void addMultiInControls(final MergeInputOutputModel model) {
        int size = Math.max(this.size, Math.max(model.getInputPathList().size(), model.getInputVariableList().size()));
        for (int i = 0; i < size; ++i) {
            String inputPath = null;
            if (i < model.getInputPathList().size()) {
                inputPath = model.getInputPathList().get(i);
            } else {
                model.getInputPathList().add(null);
            }
            String inputVariable = null;
            if (i < model.getInputVariableList().size()) {
                inputVariable = model.getInputVariableList().get(i);
            } else {
                model.getInputVariableList().add(null);
            }
            Integer typeInput = 0;
            if (i < typeInputList.size()) {
                typeInput = typeInputList.get(i);
            } else {
                typeInput = getInputType(inputPath, inputVariable);
                typeInputList.add(typeInput);
            }
            new ChooseStringOrFile(inputGroup, typeInput, inputPath, inputVariable, Messages.getString("label.filePath"), FilesSupplierMode.IN, i) {
                @Override
                public void setFileName(String fileName, int index) {
                    model.getInputPathList().set(index, fileName);
                    model.getInputVariableList().set(index, "");
                    typeInputList.set(index, getInputType(fileName, ""));
                }

                @Override
                public void setVariable(String variable, int index) {
                    model.getInputPathList().set(index, "");
                    model.getInputVariableList().set(index, variable);
                    typeInputList.set(index, getInputType("", variable));
                }

                @Override
                public void setType(Integer type, int index) {
                    model.getInputPathList().set(index, "");
                    model.getInputVariableList().set(index, "");
                    typeInputList.set(index, type);
                    updateUI();
                }
            };
            String inputAddBreak = null;
            if (i < model.getInputAddBreakList().size()) {
                inputAddBreak = model.getInputAddBreakList().get(i);
            } else {
                model.getInputAddBreakList().add(null);
            }
            final Button addBreak = new Button(inputGroup, SWT.CHECK);
            addBreak.setText(Messages.getString("label.inputAddBreak"));
            addBreak.setSelection(!"false".equals(inputAddBreak));
            final int index = i;
            addBreak.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    model.getInputAddBreakList().set(index, String.valueOf(addBreak.getSelection()));
                }
            });
        }
    }

    private Integer getInputType(String inputPath, String inputVariable) {
        Integer typeInput = 0;
        if (!Strings.isNullOrEmpty(inputVariable)) {
            typeInput = 1;
        } else if (EmbeddedFileUtils.isProcessFile(inputPath)) {
            typeInput = 2;
        }
        return typeInput;
    }

    private void initInGroup() {
        inputGroup = new Group(this, SWT.NONE);
        inputGroup.setText(Messages.getString("label.input"));
        inputGroup.setLayout(new GridLayout(3, false));
    }

    private void addOutControls(final MergeInputOutputModel model) {
        outputGroup = new Group(this, SWT.NONE);
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
            }
        });
        if (outputType == null) {
            outputType = getInputType(model.outputDir, model.outputVariable);
        }
        new ChooseStringOrFile(outputGroup, outputType, model.outputDir, model.outputVariable, Messages.getString("label.fileDir"),
                FilesSupplierMode.OUT) {
            @Override
            public void setFileName(String fileName, int index) {
                model.outputDir = fileName;
                model.outputVariable = "";
            }

            @Override
            public void setVariable(String variable, int index) {
                model.outputDir = "";
                model.outputVariable = variable;
            }

            @Override
            public void setType(Integer type, int index) {
                model.outputDir = "";
                model.outputVariable = "";
                outputType = type;
                updateUI();
            }
        };
    }

    private abstract class ChooseStringOrFile implements PropertyChangeListener {
        public abstract void setFileName(String fileName, int index);

        public abstract void setVariable(String variable, int index);

        public abstract void setType(Integer type, int index);

        private Control control = null;
        private final Composite composite;
        private final int index;

        public ChooseStringOrFile(Composite composite, Integer typeInput, String fileName, String variableName, String stringLabel,
                FilesSupplierMode mode) {
            this(composite, typeInput, fileName, variableName, stringLabel, mode, 0);
        }

        public ChooseStringOrFile(Composite composite, Integer typeInput, String fileName, String variableName, String stringLabel,
                FilesSupplierMode mode, int indexIn) {
            this.index = indexIn;
            this.composite = composite;
            final Combo combo = new Combo(composite, SWT.READ_ONLY);
            combo.add(stringLabel);
            combo.add(Messages.getString("label.fileVariable"));
            if (mode == FilesSupplierMode.IN) {
                combo.add(Messages.getString("label.processDefinitionFile"));
            }
            if ((typeInput == null && !Strings.isNullOrEmpty(variableName)) || (typeInput != null && typeInput.equals(1))) {
                combo.select(1);
                showVariable(variableName);
            } else if ((typeInput == null && EmbeddedFileUtils.isProcessFile(fileName)) || (typeInput != null && typeInput.equals(2))) {
                combo.select(2);
                showEmbeddedFile(fileName);
            } else {
                combo.select(0);
                showFileName(fileName);
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    control.dispose();
                    setType(combo.getSelectionIndex(), index);
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
                    setFileName(text.getText(), index);
                }
            });
            control = text;
            composite.layout(true, true);
        }

        private void showVariable(String variable) {
            if (control != null) {
                if (!Combo.class.isInstance(control)) {
                    control.dispose();
                } else {
                    return;
                }
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
                    setVariable(combo.getText(), index);
                }
            });
            combo.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    setVariable(combo.getText(), index);
                }
            });
            control = combo;
            composite.layout(true, true);
        }

        private void showEmbeddedFile(String path) {
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
                    final String dotExt = "." + fileExtension;
                    fileName = EmbeddedFileUtils.generateEmbeddedFileName(delegable, fileExtension).replace(dotExt, index + dotExt);
                }
            } else {
                throw new InternalApplicationException("Unexpected classtype " + delegable);
            }

            // http://sourceforge.net/p/runawfe/bugs/628/
            updateEmbeddedFileName(fileName);

            control = new TemplateFileComposite(composite, fileName, fileExtension);
            ((TemplateFileComposite) control).getEventSupport().addPropertyChangeListener(this);
            composite.layout(true, true);
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            updateEmbeddedFileName((String) event.getNewValue());
        }

        private void updateEmbeddedFileName(String fileName) {
            if (delegable instanceof GraphElement) {
                setFileName(EmbeddedFileUtils.getProcessFilePath(fileName), index);
            } else {
                throw new InternalApplicationException("Unexpected classtype " + delegable);
            }
        }

    }

}