package ru.runa.gpd.quick.formeditor;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.extension.Artifact;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.ftl.TemplateProcessor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.extension.QuickTemplateArtifact;
import ru.runa.gpd.quick.extension.QuickTemplateRegister;
import ru.runa.gpd.quick.formeditor.ui.PreviewFormWizard;
import ru.runa.gpd.quick.formeditor.ui.wizard.QuickFormVariableWizard;
import ru.runa.gpd.quick.formeditor.ui.wizard.QuickFormVariabliesToDisplayWizard;
import ru.runa.gpd.quick.formeditor.util.PresentationVariableUtils;
import ru.runa.gpd.quick.formeditor.util.QuickFormConvertor;
import ru.runa.gpd.quick.formeditor.util.QuickFormConvertor.ConverterSource;
import ru.runa.gpd.quick.formeditor.util.QuickFormXMLUtil;
import ru.runa.gpd.quick.tag.FormHashModelGpdWrap;
import ru.runa.gpd.ui.custom.DragAndDropAdapter;
import ru.runa.gpd.ui.custom.DropDownButton;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.TableViewerLocalDragAndDropSupport;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class QuickFormEditor extends EditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {
    public static final int CLOSED = 198;
    public static final int SAVED = 257;
    public static final String ID = "ru.runa.gpd.quick.formeditor.QuickFormEditor";
    private Composite editorComposite;
    private TableViewer tableViewer;
    private TableViewer propertiesTableViewer;
    private ProcessDefinition processDefinition;
    private QuickForm quickForm;
    private FormNode formNode;
    private IFile formFile;
    private IFolder definitionFolder;
    private boolean dirty;
    private String prevTemplateFileName;
    private Combo templateCombo;
    private Button previewButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button changeButton;
    private Button deleteButton;
    private Button convertButton;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);

        formFile = ((FileEditorInput) input).getFile();
        definitionFolder = (IFolder) formFile.getParent();
        IFile definitionFile = IOUtils.getProcessDefinitionFile(definitionFolder);
        this.processDefinition = ProcessCache.getProcessDefinition(definitionFile);
        if (formFile.getName().startsWith(ParContentProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            String subprocessId = formFile.getName().substring(0, formFile.getName().indexOf("."));
            processDefinition = processDefinition.getEmbeddedSubprocessById(subprocessId);
            Preconditions.checkNotNull(processDefinition, "embedded subpocess");
        }
        for (FormNode formNode : processDefinition.getChildren(FormNode.class)) {
            if (formFile.getName().equals(formNode.getFormFileName())) {
                this.formNode = formNode;
                setPartName(formNode.getName());
                break;
            }
        }
        if (formNode == null) {
            throw new InternalApplicationException("Form node not found by file name '" + formFile.getName() + "'");
        }
        quickForm = QuickFormXMLUtil.getQuickFormFromXML(formFile, formNode);

        getSite().getPage().addSelectionListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);

        addPropertyListener(new IPropertyListener() {
            @Override
            public void propertyChanged(Object source, int propId) {
                if (propId == QuickFormEditor.CLOSED) {
                    if (formFile.exists()) {
                        if (isEmpty() && !getSite().getWorkbenchWindow().getWorkbench().isClosing()) {
                            try {
                                formFile.delete(true, null);
                            } catch (CoreException e) {
                                PluginLogger.logError(e);
                            }
                        } else {
                            ValidationUtil.createOrUpdateValidation(formNode, formFile);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            InputStream content = new ByteArrayInputStream(getFormData());
            // if (!quickFormFile.exists()) {
            // quickFormFile.create(content, true, null);
            // } else {
            formFile.setContents(content, true, true, null);
            // }
            if (formNode != null) {
                if (formFile.exists() && !isEmpty()) {
                    formNode.setDirty();
                    ValidationUtil.createOrUpdateValidation(formNode, formFile);
                }
            }
            setDirty(false);
            updateButtons();
        } catch (Exception e) {
            PluginLogger.logError("Error on saving template form: '" + quickForm.getName() + "'", e);
        }
    }

    public byte[] getFormData() throws UnsupportedEncodingException, CoreException {
        return QuickFormXMLUtil.convertQuickFormToXML(definitionFolder, quickForm, formNode.getTemplateFileName());
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        editorComposite = new Composite(parent, SWT.NONE);
        editorComposite.setLayout(new GridLayout());
        editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        rebuildView(editorComposite);
    }

    private void rebuildView(Composite composite) {
        for (Control control : composite.getChildren()) {
            control.dispose();
        }

        Composite selectTemplateComposite = new Composite(composite, SWT.NONE);
        selectTemplateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        selectTemplateComposite.setLayout(new GridLayout(2, false));

        Label label = new Label(selectTemplateComposite, SWT.NONE);
        label.setText(Messages.getString("editor.list.label"));

        templateCombo = new Combo(selectTemplateComposite, SWT.BORDER | SWT.READ_ONLY);
        for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
            if (artifact.isEnabled()) {
                templateCombo.add(artifact.getLabel());
            }
        }
        templateCombo.select(0);
        templateCombo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String label = templateCombo.getText();
                setSelection(label);
            }
        });
        templateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (!Strings.isNullOrEmpty(formNode.getTemplateFileName())) {
            for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
                if (artifact.isEnabled() && artifact.getFileName().equals(formNode.getTemplateFileName())) {
                    templateCombo.setText(artifact.getLabel());
                    prevTemplateFileName = artifact.getFileName();
                    break;
                }
            }
        }

        Composite tableParamComposite = new Composite(composite, SWT.NONE);
        tableParamComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableParamComposite.setLayout(new GridLayout(2, false));

        tableViewer = new TableViewer(tableParamComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        tableViewer.setLabelProvider(new TableLabelProvider());
        tableViewer.setContentProvider(new ArrayContentProvider());
        getSite().setSelectionProvider(tableViewer);
        Table table = tableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Messages.getString("editor.table.column.tag"), Messages.getString("editor.table.column.var"),
                Messages.getString("editor.table.column.type"), Messages.getString("editor.table.column.rule") };
        int[] columnWidths = new int[] { 150, 200, 100, 100, 100 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        Composite buttonsBar = new Composite(tableParamComposite, SWT.NONE);
        GridData gridDataButton = new GridData(GridData.FILL_HORIZONTAL);
        gridDataButton.minimumWidth = 100;
        tableParamComposite.setLayoutData(gridDataButton);
        tableParamComposite.setLayout(new GridLayout(2, false));
        setTableInput();

        buttonsBar.setLayout(new GridLayout());
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        buttonsBar.setLayoutData(gridData);

        DropDownButton addButton = new DropDownButton(buttonsBar);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setText(Messages.getString("editor.button.add"));
        addButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                QuickFormVariableWizard wizard = new QuickFormVariableWizard(formNode, quickForm.getVariables(), -1);
                CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                if (dialog.open() == Window.OK) {
                    setTableInput();
                    setTableSelection(quickForm.getVariables().get(quickForm.getVariables().size() - 1));
                    setDirty(true);
                }
            }
        });
        addButton.addButton(Messages.getString("editor.button.multiple"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                QuickFormVariabliesToDisplayWizard wizard = new QuickFormVariabliesToDisplayWizard(formNode, quickForm.getVariables());
                CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                dialog.setPageSize(500, 300);
                if (dialog.open() == Window.OK) {
                    setTableInput();
                    setTableSelection(quickForm.getVariables().get(quickForm.getVariables().size() - 1));
                    setDirty(true);
                }
            }
        });
        changeButton = SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.change"), new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                QuickFormGpdVariable row = (QuickFormGpdVariable) selection.getFirstElement();
                for (QuickFormGpdVariable variableDef : quickForm.getVariables()) {
                    if (variableDef.getName().equals(row.getName())) {
                        QuickFormVariableWizard wizard = new QuickFormVariableWizard(formNode, quickForm.getVariables(), quickForm.getVariables()
                                .indexOf(variableDef));
                        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                        if (dialog.open() == Window.OK) {
                            setTableInput();
                            setDirty(true);
                            tableViewer.setSelection(selection);
                        }
                        break;
                    }
                }
            }
        });
        previewButton = SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.preview"), new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String filename = formNode.getTemplateFileName();
                Bundle bundle = QuickTemplateRegister.getBundle(filename);
                String quickTemplate = QuickFormXMLUtil.getTemplateFromRegister(bundle, filename);
                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("variables", quickForm.getVariables());
                variables.put("task", "");
                for (QuickFormGpdProperty quickFormGpdProperty : quickForm.getProperties()) {
                    variables.put(quickFormGpdProperty.getName(), quickFormGpdProperty.getValue() == null ? "" : quickFormGpdProperty.getValue());
                }
                FormHashModelGpdWrap quickModel = new FormHashModelGpdWrap(null, new MapVariableProvider(variables), null);
                String ftlTemplate = TemplateProcessor.process(formFile.getFullPath().toString(), quickTemplate, quickModel);
                MapVariableProvider ftlVariableProvider = new MapVariableProvider(new HashMap<String, Object>());
                for (QuickFormGpdVariable quickFormGpdVariable : quickForm.getVariables()) {
                    Variable variable = VariableUtils.getVariableByName(processDefinition, quickFormGpdVariable.getName());
                    if (variable == null && formNode instanceof MultiTaskState) {
                        for (VariableMapping variableMapping : ((MultiTaskState) formNode).getVariableMappings()) {
                            if (Objects.equal(variableMapping.getMappedName(), quickFormGpdVariable.getName())) {
                                if (variableMapping.isMultiinstanceLink()) {
                                    Variable listVariable = VariableUtils.getVariableByName(processDefinition, variableMapping.getName());
                                    String format = listVariable.getFormatComponentClassNames()[0];
                                    VariableUserType userType = processDefinition.getVariableUserType(format);
                                    variable = new Variable(quickFormGpdVariable.getName(), quickFormGpdVariable.getName(), format, userType);
                                } else {
                                    variable = VariableUtils.getVariableByName(processDefinition, variableMapping.getName());
                                }
                                break;
                            }
                        }
                        if (variable == null) {
                            // prevent NPE
                            continue;
                        }
                    }
                    String defaultValue = PresentationVariableUtils.getPresentationValue(variable.getFormat());
                    Object value = null;
                    if (defaultValue != null) {
                        value = TypeConversionUtil.convertTo(ClassLoaderUtil.loadClass(variable.getJavaClassName()), defaultValue);
                    }
                    VariableDefinition variableDefinition = new VariableDefinition(quickFormGpdVariable.getName(), null);
                    variableDefinition.setFormat(variable.getFormat());
                    WfVariable wfVariable = new WfVariable(variableDefinition, value);
                    ftlVariableProvider.add(wfVariable);
                }
                FormHashModelGpdWrap ftlModel = new FormHashModelGpdWrap(null, ftlVariableProvider, null);
                String form = TemplateProcessor.process(formFile.getFullPath().toString() + "_2", ftlTemplate, ftlModel);
                IFile formCssFile = definitionFolder.getFile(ParContentProvider.FORM_CSS_FILE_NAME);
                String styles = formCssFile.exists() ? IOUtils.readStream(formCssFile.getContents()) : null;
                PreviewFormWizard wizard = new PreviewFormWizard(form, styles);
                CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                dialog.open();
            }
        });
        moveUpButton = SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.up"), new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                QuickFormGpdVariable row = (QuickFormGpdVariable) selection.getFirstElement();
                for (QuickFormGpdVariable variableDef : quickForm.getVariables()) {
                    if (variableDef.getName().equals(row.getName())) {
                        int index = quickForm.getVariables().indexOf(variableDef);
                        if (index > 0) {
                            quickForm.getVariables().remove(index);
                            quickForm.getVariables().add(index - 1, variableDef);
                            setTableInput();
                            setDirty(true);
                            tableViewer.setSelection(selection);
                            break;
                        }
                    }
                }
            }
        });
        moveDownButton = SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.down"), new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                QuickFormGpdVariable row = (QuickFormGpdVariable) selection.getFirstElement();
                for (QuickFormGpdVariable variableDef : quickForm.getVariables()) {
                    if (variableDef.getName().equals(row.getName())) {
                        int index = quickForm.getVariables().indexOf(variableDef);
                        if (index < quickForm.getVariables().size() - 1) {
                            quickForm.getVariables().remove(index);
                            quickForm.getVariables().add(index + 1, variableDef);
                            setTableInput();
                            setDirty(true);
                            tableViewer.setSelection(selection);
                            break;
                        }
                    }
                }
            }
        });
        deleteButton = SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.delete"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                quickForm.getVariables().removeAll(selection.toList());
                setTableInput();
                setDirty(true);
            }
        });
        SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.rules"), new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
            }
        }).setEnabled(false);
        convertButton = SWTUtils.createButtonFillHorizontal(buttonsBar, Messages.getString("editor.button.convert"), new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                QuickFormConvertor.convertQuickFormToSimple(new ConverterSource() {
                    @Override
                    public IFile getQuickFormFile() {
                        return formFile;
                    }

                    @Override
                    public QuickForm getQuickForm() {
                        return quickForm;
                    }

                    @Override
                    public ProcessDefinition getProcessDefinition() {
                        return processDefinition;
                    }

                    @Override
                    public FormNode getFormNode() {
                        return formNode;
                    }
                });
            }
        });
        TableViewerLocalDragAndDropSupport.enable(tableViewer, new DragAndDropAdapter<QuickFormGpdVariable>() {

            @Override
            public void onDropElement(QuickFormGpdVariable beforeElement, QuickFormGpdVariable element) {
                if (quickForm.getVariables().remove(element)) {
                    int index = quickForm.getVariables().indexOf(beforeElement);
                    quickForm.getVariables().add(index, element);
                }
            }

            @Override
            public void onDrop(QuickFormGpdVariable beforeElement, List<QuickFormGpdVariable> elements) {
                super.onDrop(beforeElement, elements);
                setTableInput();
                setDirty(true);
            }
        });

        tableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                updateButtons();
            }
        });
        updateButtons();

        Composite tableParameterComposite = new Composite(composite, SWT.NONE);
        tableParameterComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        tableParameterComposite.setLayout(new GridLayout(1, false));

        Label labelParameters = new Label(tableParameterComposite, SWT.NONE);
        labelParameters.setText(Messages.getString("editor.paramtable.label"));

        propertiesTableViewer = new TableViewer(tableParameterComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData propertiesGridData = new GridData(GridData.FILL_BOTH);
        propertiesGridData.heightHint = 120;
        propertiesTableViewer.getControl().setLayoutData(propertiesGridData);
        propertiesTableViewer.setLabelProvider(new PropertyTableLabelProvider());
        propertiesTableViewer.setContentProvider(new ArrayContentProvider());
        final Table propertiesTable = propertiesTableViewer.getTable();
        propertiesTable.setHeaderVisible(true);
        propertiesTable.setLinesVisible(true);
        String[] propertiesColumnNames = new String[] { Messages.getString("editor.paramtable.column1"),
                Messages.getString("editor.paramtable.column2") };
        int[] propertiesColumnWidths = new int[] { 150, 750 };
        int[] propertiesColumnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
        for (int i = 0; i < propertiesColumnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(propertiesTable, propertiesColumnAlignments[i]);
            tableColumn.setText(propertiesColumnNames[i]);
            tableColumn.setWidth(propertiesColumnWidths[i]);
        }
        final TableEditor editor = new TableEditor(propertiesTable);
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        // editing the second column
        final int EDITABLECOLUMN = 1;

        propertiesTable.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                Control oldEditor = editor.getEditor();
                if (oldEditor != null) {
                    oldEditor.dispose();
                }

                TableItem item = (TableItem) e.item;
                if (item == null) {
                    return;
                }

                Text newEditor = new Text(propertiesTable, SWT.NONE);
                newEditor.setText(item.getText(EDITABLECOLUMN));
                newEditor.addModifyListener(new LoggingModifyTextAdapter() {

                    @Override
                    protected void onTextChanged(ModifyEvent e) throws Exception {
                        Text text = (Text) editor.getEditor();
                        editor.getItem().setText(EDITABLECOLUMN, text.getText());
                        for (QuickFormGpdProperty quickFormGpdProperty : quickForm.getProperties()) {
                            if (quickFormGpdProperty.getLabel().equals(editor.getItem().getText(0))) {
                                quickFormGpdProperty.setValue(text.getText());
                                break;
                            }
                        }
                        setDirty(true);
                    }
                });
                List<String> variableNames = processDefinition.getVariableNames(true);
                new InsertVariableTextMenuDetectListener(newEditor, variableNames);

                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item, EDITABLECOLUMN);
            }
        });

        setPropertiesTableInput();
        setDefaultSelection();
        composite.layout(true, true);
    }

    private void setDefaultSelection() {
        if (templateCombo.getItemCount() != 0) {
            int selectionIndex = templateCombo.getSelectionIndex();
            setSelection(templateCombo.getItem(selectionIndex));
        } else {
            setSelection(null);
        }
    }

    private void updateButtons() {
        boolean isTemplateValid = !Strings.isNullOrEmpty(formNode.getTemplateFileName());
        List<?> selected = ((IStructuredSelection) tableViewer.getSelection()).toList();
        previewButton.setEnabled(isTemplateValid);
        changeButton.setEnabled(selected.size() == 1);
        moveUpButton.setEnabled(selected.size() == 1);
        moveDownButton.setEnabled(selected.size() == 1);
        deleteButton.setEnabled(selected.size() > 0);
        updateConvertButton(isTemplateValid);
    }

    private void updateConvertButton(boolean isTemplateValid) {
        convertButton.setEnabled(isTemplateValid && !isDirty());
    }

    private void setSelection(String label) {
        String filename = null;
        for (QuickTemplateArtifact artifact : QuickTemplateRegister.getInstance().getAll(true)) {
            if (label != null && artifact.isEnabled() && label.equalsIgnoreCase(artifact.getLabel())) {
                filename = artifact.getFileName();
                break;
            }
        }

        if ((prevTemplateFileName == null && filename == null)
                || (!Strings.isNullOrEmpty(prevTemplateFileName) && prevTemplateFileName.equals(filename))) {
            return;
        }

        if (filename != null && filename.trim().length() > 0) {
            Bundle bundle = QuickTemplateRegister.getBundle(filename);
            String templateHtml = QuickFormXMLUtil.getTemplateFromRegister(bundle, filename);
            formNode.setTemplateFileName(filename);
            quickForm.setDelegationConfiguration(templateHtml);

            quickForm.getProperties().clear();
            QuickTemplateArtifact quickTemplateArtifact = QuickTemplateRegister.getInstance().getArtifactByFileName(filename);
            if (quickTemplateArtifact != null) {
                List<Artifact> parameters = quickTemplateArtifact.getParameters();
                if (parameters != null && parameters.size() > 0) {

                    for (Artifact parameter : parameters) {
                        QuickFormGpdProperty gpdProperty = new QuickFormGpdProperty();
                        gpdProperty.setName(parameter.getName());
                        gpdProperty.setLabel(parameter.getLabel());
                        quickForm.getProperties().add(gpdProperty);
                    }
                }
            }

            setPropertiesTableInput();
        }

        if (!Strings.isNullOrEmpty(prevTemplateFileName)) {
            if (!QuickFormEditorUtil.isTemplateUsingInForms(processDefinition, formNode, prevTemplateFileName)) {
                IFile confFile = definitionFolder.getFile(prevTemplateFileName);
                if (confFile.exists()) {
                    try {
                        confFile.delete(true, null);
                    } catch (CoreException e) {
                    }
                }
            }
        }

        prevTemplateFileName = filename;
        setDirty(true);
        updateConvertButton(!Strings.isNullOrEmpty(formNode.getTemplateFileName()));
    }

    private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            QuickFormGpdVariable variableDef = (QuickFormGpdVariable) element;
            switch (index) {
            case 0:
                if (ComponentTypeRegistry.has(variableDef.getTagName())) {
                    ComponentType tag = ComponentTypeRegistry.getNotNull(variableDef.getTagName());
                    return tag.getLabel();
                }
                return variableDef.getTagName();
            case 1:
                return variableDef.getName();
            case 2:
                return variableDef.getFormatLabel();
            case 3:
                return "";
            default:
                return "unknown " + index;
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private static class PropertyTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            QuickFormGpdProperty propertyDef = (QuickFormGpdProperty) element;
            switch (index) {
            case 0:
                return propertyDef.getLabel();
            case 1:
                return propertyDef.getValue();
            }
            return "";
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyNames.PROPERTY_DIRTY.equals(evt.getPropertyName())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        EditorUtils.closeEditorIfRequired(event, formFile, this);
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    }

    private void setTableInput() {
        tableViewer.setInput(quickForm.getVariables());
    }

    private void setPropertiesTableInput() {
        propertiesTableViewer.setInput(quickForm.getProperties());
    }

    private void setTableSelection(QuickFormGpdVariable variableDef) {
        IStructuredSelection selection = new StructuredSelection(variableDef);
        tableViewer.setSelection(selection);
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        if (this.dirty != dirty) {
            this.dirty = dirty;
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public void dispose() {
        firePropertyChange(CLOSED);
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        super.dispose();
    }

    public boolean isEmpty() {
        return quickForm == null || quickForm.getVariables().isEmpty();
    }

}
