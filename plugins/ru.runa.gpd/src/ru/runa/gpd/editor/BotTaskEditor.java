package ru.runa.gpd.editor;

import com.google.common.collect.Lists;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.ui.wizard.BotTaskParamDefWizard;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.gpd.util.XmlUtil;

public class BotTaskEditor extends EditorPart implements ISelectionListener, IResourceChangeListener, PropertyChangeListener {
    public static final String ID = "ru.runa.gpd.editor.BotTaskEditor";
    public static final String DocxHandlerID = "ru.runa.wfe.office.doc.DocxHandler";
    private BotTask botTask;
    private boolean dirty;
    private IFile botTaskFile;
    private Composite editorComposite;
    private Text handlerText;
    private Button chooseTaskHandlerClassButton;
    private Button editConfigurationButton;
    private StyledText configurationText;
    private TableViewer inputParamTableViewer;
    private TableViewer outputParamTableViewer;
    private Boolean enableReadDocxParametersButtons;
    private Boolean enableReadDocxButton;
    private Button addParameterButton;
    private Button editParameterButton;
    private Button deleteParameterButton;
    private Button readDocxParametersButton;
    private String embeddedFileName;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        FileEditorInput fileInput = (FileEditorInput) input;
        botTaskFile = fileInput.getFile();
        try {
            botTask = BotCache.getBotTaskNotNull(botTaskFile);
        } catch (Exception e) {
            throw new PartInitException("", e);
        }
        if (botTask.getType() != BotTaskType.SIMPLE) {
            this.setTitleImage(SharedImages.getImage("icons/bot_task_formal.gif"));
        }
        setPartName(botTask.getName());
        setDirty(false);
        getSite().getPage().addSelectionListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            WorkspaceOperations.saveBotTask(botTaskFile, botTask);
            setTitleImage(SharedImages.getImage(botTask.getType() == BotTaskType.SIMPLE ? "icons/bot_task.gif" : "icons/bot_task_formal.gif"));
            setDirty(false);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void dispose() {
        // If bot task has been changed but not saved we should reload it from
        // XML
        if (isDirty()) {
            try {
                BotCache.invalidateBotTask(botTaskFile, botTask);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
        super.dispose();
    }

    @Override
    public void doSaveAs() {
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
        configurationText = null;
        addParameterButton = null;
        editParameterButton = null;
        deleteParameterButton = null;
        readDocxParametersButton = null;

        createTaskHandlerClassField(composite);
        if (botTask.getType() == BotTaskType.PARAMETERIZED) {
            createConfTableViewer(composite, ParamDefGroup.NAME_INPUT);
            createConfTableViewer(composite, ParamDefGroup.NAME_OUTPUT);
        } else {
            ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

            Composite innerComposite = new Composite(scrolledComposite, SWT.NONE);
            innerComposite.setLayout(new GridLayout());
            createParamteresFields(composite, innerComposite);
            if (!isBotDocxHandlerEnhancement()) {
                createConfigurationFields(innerComposite);
            }
            createConfigurationArea(innerComposite);
            scrolledComposite.setMinSize(SWT.DEFAULT, 700);
            scrolledComposite.setContent(innerComposite);
        }
        populateFields();
        composite.layout(true, true);
    }

    private void createTaskHandlerClassField(final Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaComposite.setLayout(new GridLayout(3, false));
        Label label = new Label(dynaComposite, SWT.NONE);
        GridData gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 150;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.taskHandler"));
        handlerText = new Text(dynaComposite, SWT.BORDER);
        handlerText.setEditable(false);
        handlerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        chooseTaskHandlerClassButton = new Button(dynaComposite, SWT.NONE);
        chooseTaskHandlerClassButton.setText(Localization.getString("button.choose"));
        chooseTaskHandlerClassButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(HandlerArtifact.TASK_HANDLER, handlerText.getText());
                String className = dialog.openDialog();
                if (className != null) {
                    boolean taskHandlerParameterized = BotTaskUtils.isTaskHandlerParameterized(className);
                    handlerText.setText(className);
                    botTask.setDelegationClassName(className);
                    if (taskHandlerParameterized) {
                        DelegableProvider provider = HandlerRegistry.getProvider(className);
                        String xml = XmlUtil.getParamDefConfig(provider.getBundle(), className);
                        botTask.setParamDefConfig(ParamDefConfig.parse(xml));
                        botTask.setType(BotTaskType.PARAMETERIZED);
                    } else {
                        botTask.setType(BotTaskType.EXTENDED);
                        botTask.setParamDefConfig(BotTaskUtils.createEmptyParamDefConfig());
                    }
                    botTask.setDelegationConfiguration("");
                    setDirty(true);
                    rebuildView(parent);
                }
            }
        });
    }

    private void createParamteresFields(final Composite mainComposite, Composite parent) {

        boolean isBotDocxHandlerEnhancement = isBotDocxHandlerEnhancement();

        if (!isBotDocxHandlerEnhancement) {
            Composite dynaComposite = new Composite(parent, SWT.NONE);
            dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            dynaComposite.setLayout(new GridLayout(3, false));

            createButtonParametersDisabler(mainComposite, dynaComposite);
        }

        if (botTask.getType() == BotTaskType.EXTENDED) {

            Object docxModel = null;
            if (isBotDocxHandlerEnhancement) {
                DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                docxModel = provider.showEmbeddedConfigurationDialog(parent, botTask,
                        new DocxDialogEnhancementMode(DocxDialogEnhancementMode.DOCX_SHOW_INPUT) {
                            @Override
                            public void reloadXmlFromModel(String newConfiguration, String embeddedFileName, Boolean enableReadDocxButton,
                                    Boolean enableDocxMode) {
                                reloadDialogXmlFromModel(newConfiguration, embeddedFileName, enableReadDocxButton, enableDocxMode);
                            }
                        });
            }

            createConfTableViewer(parent, ParamDefGroup.NAME_INPUT);

            if (isBotDocxHandlerEnhancement) {
                DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                DocxDialogEnhancementMode docxDialogEnhancementMode = new DocxDialogEnhancementMode(DocxDialogEnhancementMode.DOCX_SHOW_OUTPUT) {
                    @Override
                    public void reloadXmlFromModel(String newConfiguration, String embeddedFileName, Boolean enableReadDocxButton,
                            Boolean enableDocxMode) {
                        reloadDialogXmlFromModel(newConfiguration, embeddedFileName, enableReadDocxButton, enableDocxMode);
                    }
                };

                docxDialogEnhancementMode.docxModel = docxModel;
                provider.showEmbeddedConfigurationDialog(parent, botTask, docxDialogEnhancementMode);
            } else {
                createConfTableViewer(parent, ParamDefGroup.NAME_OUTPUT);
            }
        }

        if (isBotDocxHandlerEnhancement) {
            Composite dynaComposite = new Composite(parent, SWT.NONE);
            dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            dynaComposite.setLayout(new GridLayout(3, false));

            createButtonParametersDisabler(mainComposite, dynaComposite);
        }
    }

    private void reloadDialogXmlFromModel(String newConfiguration, String embeddedFileName, Boolean enableReadDocxButton, Boolean enableDocxMode) {
        if (configurationText != null && newConfiguration != null) {
            configurationText.setText(newConfiguration);
            botTask.setDelegationConfiguration(newConfiguration);
            setDirty(true);
        }
        if (embeddedFileName != null) {
            this.embeddedFileName = embeddedFileName;
        }

        if (enableReadDocxButton != null) {
            this.enableReadDocxButton = enableReadDocxButton;
            if (null != readDocxParametersButton) {
                readDocxParametersButton.setEnabled(enableReadDocxButton);
            }
        }

        if (enableDocxMode != null) {
            this.enableReadDocxParametersButtons = enableDocxMode;
            if (null != addParameterButton) {
                addParameterButton.setEnabled(!enableDocxMode);
            }
            if (null != deleteParameterButton) {
                deleteParameterButton.setEnabled(!enableDocxMode);
            }
            if (null != editParameterButton) {
                editParameterButton.setEnabled(true);
            }
        }
    }

    private Map<String, Integer> getVariableNamesFromDocxTemplate(InputStream templateInputStream) {
        Map<String, Integer> variablesMap = new HashMap<String, Integer>();
        try (XWPFDocument document = new XWPFDocument(templateInputStream)) {
            JOptionPane.showMessageDialog(null, "DOCX Stream opened.");
            for (XWPFHeader header : document.getHeaderList()) {
                getVariableNamesFromDocxBodyElements(header.getBodyElements(), variablesMap);
            }
            getVariableNamesFromDocxBodyElements(document.getBodyElements(), variablesMap);
            for (XWPFFooter footer : document.getFooterList()) {
                getVariableNamesFromDocxBodyElements(footer.getBodyElements(), variablesMap);
            }
        } catch (Throwable th) {
            JOptionPane.showMessageDialog(null, "Can't open DOCX Stream " + th.getMessage());
            th.printStackTrace();
        }

        return variablesMap;
    }

    private void getVariableNamesFromDocxBodyElements(List<IBodyElement> bodyElements, Map<String, Integer> variablesMap) {
        List<XWPFParagraph> paragraphs = Lists.newArrayList();
        for (IBodyElement bodyElement : new ArrayList<IBodyElement>(bodyElements)) {
            if (bodyElement instanceof XWPFParagraph) {
                paragraphs.add((XWPFParagraph) bodyElement);
                continue;
            }
            /*
             * if (!paragraphs.isEmpty()) { getVariableNamesFromDocxParagraphs(paragraphs, variablesList); paragraphs.clear(); }
             */
            // if (bodyElement instanceof XWPFTable) {
            // XWPFTable table = (XWPFTable) bodyElement;
            // List<XWPFTableRow> rows = table.getRows();
            // for (XWPFTableRow row : Lists.newArrayList(rows)) {
            // List<XWPFTableCell> cells = row.getTableCells();
            // // try to expand cells by column
            // TableExpansionOperation tableExpansionOperation = new TableExpansionOperation(row);
            // for (int columnIndex = 0; columnIndex < cells.size(); columnIndex++) {
            // final XWPFTableCell cell = cells.get(columnIndex);
            // ColumnExpansionOperation operation = DocxUtils.parseIterationOperation(config, variableProvider, cell.getText(),
            // new ColumnExpansionOperation());
            // if (operation != null && operation.isValid()) {
            // tableExpansionOperation.addOperation(columnIndex, operation);
            // } else {
            // operation = new ColumnSetValueOperation();
            // operation.setContainerValue(cell.getText());
            // tableExpansionOperation.addOperation(columnIndex, operation);
            // }
            // String text0 = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, 0);
            // if (!java.util.Objects.equals(text0, cell.getText())) {
            // DocxUtils.setCellText(cell, text0);
            // }
            // }
            // if (tableExpansionOperation.getRows() == 0) {
            // for (XWPFTableCell cell : cells) {
            // DocxUtils.replaceInParagraphs(config, variableProvider, cell.getParagraphs());
            // }
            // } else {
            // int templateRowIndex = table.getRows().indexOf(tableExpansionOperation.getTemplateRow());
            // for (int rowIndex = 1; rowIndex < tableExpansionOperation.getRows(); rowIndex++) {
            // XWPFTableRow dynamicRow = table.insertNewTableRow(templateRowIndex + rowIndex);
            // for (int columnIndex = 0; columnIndex < tableExpansionOperation.getTemplateRow().getTableCells().size(); columnIndex++) {
            // dynamicRow.createCell();
            // }
            // for (int columnIndex = 0; columnIndex < dynamicRow.getTableCells().size(); columnIndex++) {
            // String text = tableExpansionOperation.getStringValue(config, variableProvider, columnIndex, rowIndex);
            // DocxUtils.setCellText(dynamicRow.getCell(columnIndex), text, tableExpansionOperation.getTemplateCell(columnIndex));
            // if (OfficeProperties.getDocxPlaceholderVMerge().equals(text)) {
            // CTTcPr tcPr = dynamicRow.getCell(columnIndex).getCTTc().getTcPr();
            // tcPr.addNewVMerge().setVal(STMerge.CONTINUE);
            //
            // int restartVMergeRowIndex = table.getRows().indexOf(dynamicRow) - 1;
            // if (restartVMergeRowIndex >= 0) {
            // XWPFTableRow restartRow = table.getRow(restartVMergeRowIndex);
            // CTTcPr previousTcPr = restartRow.getCell(columnIndex).getCTTc().getTcPr();
            // if (previousTcPr.getVMerge() == null) {
            // previousTcPr.addNewVMerge().setVal(STMerge.RESTART);
            // }
            // }
            // }
            // }
            // }
            // }
            // }
            // }
        }
        if (!paragraphs.isEmpty()) {
            getVariableNamesFromDocxParagraphs(paragraphs, variablesMap);
            paragraphs.clear();
        }
    }

    public static final String PLACEHOLDER_START = "${";
    public static final String PLACEHOLDER_END = "}";
    // public static final String CLOSING_PLACEHOLDER_START = PLACEHOLDER_START + "/";

    private void getVariableNamesFromDocxParagraphs(List<XWPFParagraph> paragraphs, Map<String, Integer> variablesMap) {
        for (XWPFParagraph paragraph : Lists.newArrayList(paragraphs)) {
            String paragraphText = paragraph.getText();
            JOptionPane.showMessageDialog(null, paragraphText);
            while (true) {
                if (!paragraphText.contains(PLACEHOLDER_START)) {
                    break;
                }
                paragraphText = paragraphText.substring(paragraphText.indexOf(PLACEHOLDER_START) + PLACEHOLDER_START.length());
                if (!paragraphText.contains(PLACEHOLDER_END)) {
                    break;
                }
                String var = paragraphText.substring(0, paragraphText.indexOf(PLACEHOLDER_END));

                if (!variablesMap.containsKey(var)) {
                    variablesMap.put(var, 1);
                    JOptionPane.showMessageDialog(null, "Var: " + var);
                }

                paragraphText = paragraphText.substring(paragraphText.indexOf(PLACEHOLDER_END) + PLACEHOLDER_END.length());
            }
        }
    }

    private void createButtonParametersDisabler(final Composite mainComposite, final Composite dynaComposite) {

        Label label = new Label(dynaComposite, SWT.NONE);
        GridData gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 150;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.params"));
        label.setToolTipText(Localization.getString("BotTaskEditor.formalParams"));
        Button button = new Button(dynaComposite, SWT.NONE);
        button.setLayoutData(new GridData(SWT.BEGINNING));
        button.setText(Localization.getString(botTask.getType() == BotTaskType.SIMPLE ? "button.parameters.enable" : "button.parameters.disable"));
        button.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                switch (botTask.getType()) {
                case SIMPLE:
                    botTask.setType(BotTaskType.EXTENDED);
                    botTask.setParamDefConfig(BotTaskUtils.createEmptyParamDefConfig());
                    setDirty(true);
                    rebuildView(mainComposite);
                    break;
                default:
                case EXTENDED:
                    if (Dialogs.confirm(Localization.getString("button.parameters.disable"))) {
                        botTask.setType(BotTaskType.SIMPLE);
                        botTask.setParamDefConfig(null);
                        setDirty(true);
                        rebuildView(mainComposite);
                    }
                    break;
                }
            }
        });
        button.setEnabled(!botTask.getDelegationClassName().isEmpty());
        button = new Button(dynaComposite, SWT.NONE);
        button.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_LCL_LINKTO_HELP));
        button.setToolTipText(Localization.getString("label.menu.help"));
        button.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                Shell shell = new Shell(mainComposite.getShell(), SWT.CLOSE | SWT.RESIZE | SWT.SYSTEM_MODAL);
                shell.setSize(600, 400);
                shell.setLayout(new GridLayout());
                shell.setText(Localization.getString("label.menu.help"));
                Label help = new Label(shell, SWT.WRAP);
                help.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                help.setText(Localization.getString("BotTaskEditor.formalParams"));
                SWTUtils.createLink(shell, Localization.getString("label.menu.moreDetails"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser()
                                .openURL(new URL(Localization.getString("BotTaskEditor.formalParams.help")));
                    }
                });
                shell.open();
            }
        });
    }

    private boolean isBotDocxHandlerEnhancement() {
        return null != botTask && DialogEnhancement.isOn() && 0 == botTask.getDelegationClassName().compareTo(DocxHandlerID);
    }

    private void createConfigurationFields(Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaComposite.setLayout(new GridLayout(2, false));
        Label label = new Label(dynaComposite, SWT.NONE);
        GridData gridData = new GridData(GridData.BEGINNING);
        gridData.widthHint = 150;
        label.setLayoutData(gridData);
        label.setText(Localization.getString("BotTaskEditor.configuration"));
        editConfigurationButton = new Button(dynaComposite, SWT.NONE);
        editConfigurationButton.setLayoutData(new GridData(GridData.BEGINNING));
        editConfigurationButton.setText(Localization.getString("button.change"));
        editConfigurationButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                String newConfiguration = provider.showConfigurationDialog(botTask);
                if (newConfiguration != null) {
                    configurationText.setText(newConfiguration);
                    botTask.setDelegationConfiguration(newConfiguration);
                    setDirty(true);
                }
            }
        });
        editConfigurationButton.setEnabled(!botTask.getDelegationClassName().isEmpty());
    }

    private void createConfigurationArea(Composite parent) {
        Composite dynaComposite = new Composite(parent, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        dynaComposite.setLayout(new GridLayout());
        configurationText = new StyledText(dynaComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        configurationText.setLineSpacing(2);
        configurationText.setEditable(false);
        configurationText.setLayoutData(new GridData(GridData.FILL_BOTH));
        if (botTask.isDelegationConfigurationInXml()) {
            configurationText.addLineStyleListener(new XmlHighlightTextStyling());
        }
    }

    private void createConfTableViewer(Composite parent, final String parameterType) {
        Composite dynaConfComposite = new Composite(parent, SWT.NONE);
        dynaConfComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaConfComposite.setLayout(new GridLayout());
        Label descriptionLabel = new Label(dynaConfComposite, SWT.NONE);
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            descriptionLabel.setText(Localization.getString("ParamDefGroup.group.input"));
        } else {
            descriptionLabel.setText(Localization.getString("ParamDefGroup.group.output"));
        }
        descriptionLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        TableViewer confTableViewer;
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            inputParamTableViewer = new TableViewer(dynaConfComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            confTableViewer = inputParamTableViewer;
        } else {
            outputParamTableViewer = new TableViewer(dynaConfComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);
            confTableViewer = outputParamTableViewer;
        }
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 120;
        confTableViewer.getControl().setLayoutData(gridData);
        Table table = confTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("BotTaskEditor.name"), Localization.getString("BotTaskEditor.type"),
                Localization.getString("BotTaskEditor.required"), Localization.getString("BotTaskEditor.useVariable") };
        int[] columnWidths = new int[] { 400, 200, 100, 100 };
        int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT, SWT.LEFT, SWT.LEFT };

        int columnNamesLength = isBotDocxHandlerEnhancement() ? columnNames.length - 1 : columnNames.length;

        for (int i = 0; i < columnNamesLength; i++) {
            TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(columnWidths[i]);
        }
        confTableViewer.setLabelProvider(new TableLabelProvider());
        confTableViewer.setContentProvider(new ArrayContentProvider());
        setTableInput(parameterType);
        if ((botTask.getType() != BotTaskType.PARAMETERIZED)) {
            createConfTableButtons(dynaConfComposite, confTableViewer, parameterType);
        }
    }

    private void createConfTableButtons(Composite dynaConfComposite, TableViewer confTableViewer, final String parameterType) {
        Composite buttonArea = new Composite(dynaConfComposite, SWT.NONE);
        buttonArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        boolean isBotDocxHandler = isBotDocxHandlerEnhancement();

        buttonArea.setLayout(new GridLayout(isBotDocxHandler ? 4 : 3, false));

        addParameterButton = new Button(buttonArea, SWT.NONE);
        addParameterButton.setText(Localization.getString("button.add"));
        addParameterButton.setEnabled(!isBotDocxHandler && botTask.getType() != BotTaskType.PARAMETERIZED);
        addParameterButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        BotTaskParamDefWizard wizard = new BotTaskParamDefWizard(group, null);
                        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                        if (dialog.open() == Window.OK) {
                            setTableInput(parameterType);
                            setDirty(true);
                        }
                    }
                }
            }
        });

        editParameterButton = new Button(buttonArea, SWT.NONE);
        editParameterButton.setText(Localization.getString("button.edit"));
        editParameterButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED
                && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        editParameterButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        IStructuredSelection selection = (IStructuredSelection) getParamTableViewer(parameterType).getSelection();
                        String[] row = (String[]) selection.getFirstElement();
                        if (row == null) {
                            return;
                        }
                        for (ParamDef paramDef : group.getParameters()) {
                            if (paramDef.getName().equals(row[0])) {
                                BotTaskParamDefWizard wizard = new BotTaskParamDefWizard(group, paramDef);
                                CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                                if (dialog.open() == Window.OK) {
                                    setTableInput(parameterType);
                                    setDirty(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        deleteParameterButton = new Button(buttonArea, SWT.NONE);
        deleteParameterButton.setText(Localization.getString("button.delete"));
        deleteParameterButton.setEnabled(!isBotDocxHandler && botTask.getType() != BotTaskType.PARAMETERIZED
                && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        deleteParameterButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        IStructuredSelection selection = (IStructuredSelection) getParamTableViewer(parameterType).getSelection();
                        String[] row = (String[]) selection.getFirstElement();
                        if (row == null) {
                            return;
                        }
                        for (ParamDef paramDef : group.getParameters()) {
                            if (paramDef.getName().equals(row[0])) {
                                group.getParameters().remove(paramDef);
                                setTableInput(parameterType);
                                setDirty(true);
                                break;
                            }
                        }
                    }
                }
            }
        });

        if (isBotDocxHandler) {
            readDocxParametersButton = new Button(buttonArea, SWT.NONE);
            readDocxParametersButton.setText(Localization.getString("button.read.docx"));
            readDocxParametersButton.setEnabled(enableReadDocxParametersButtons != null ? enableReadDocxParametersButtons : true);
            readDocxParametersButton.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    if (embeddedFileName == null || embeddedFileName.isEmpty() || !EmbeddedFileUtils.isBotTaskFile(embeddedFileName)) {
                        return;
                    }
                    IFile file = EmbeddedFileUtils.getProcessFile(embeddedFileName);
                    if (null == file || !file.exists()) {
                        return;
                    }

                    try (InputStream inputStream = file.getContents()) {
                        if (null == inputStream) {
                            return;
                        }
                        Map<String, Integer> variablesMap = getVariableNamesFromDocxTemplate(inputStream);

                        for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                            if (ParamDefGroup.NAME_INPUT.equals(group.getName())) {
                                List<ParamDef> params = group.getParameters();

                                for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                                    ParamDef pd = new ParamDef(entry.getKey(), entry.getKey());
                                    List<String> formats = pd.getFormatFilters();
                                    formats.add("java.lang.Object");
                                    params.add(pd);
                                }
                                setDirty(true);
                                break;
                            }
                        }

                        setTableInput(ParamDefGroup.NAME_INPUT);

                        // // TableViewer confTableViewer = getParamTableViewer(ParamDefGroup.NAME_INPUT);
                        // List<ParamDef> paramDefs = new ArrayList<ParamDef>();
                        // for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                        // if (ParamDefGroup.NAME_INPUT.equals(group.getName())) {
                        // paramDefs.addAll(group.getParameters());
                        //
                        // for (ParamDef pd : paramDefs) {
                        // List<String> hh = pd.getFormatFilters();
                        // int t = 0;
                        // t++;
                        // }
                        // }
                        // }

                    }

                    // for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    // if (parameterType.equals(group.getName())) {
                    // IStructuredSelection selection = (IStructuredSelection) getParamTableViewer(parameterType).getSelection();
                    // String[] row = (String[]) selection.getFirstElement();
                    // if (row == null) {
                    // return;
                    // }
                    // for (ParamDef paramDef : group.getParameters()) {
                    // if (paramDef.getName().equals(row[0])) {
                    // group.getParameters().remove(paramDef);
                    // setTableInput(parameterType);
                    // setDirty(true);
                    // break;
                    // }
                    // }
                    // }
                    // }
                }
            });
        }

        confTableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                addParameterButton.setEnabled(null != enableReadDocxParametersButtons ? !enableReadDocxParametersButtons : !isBotDocxHandler);
                editParameterButton.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED && selection.getFirstElement() != null);
                deleteParameterButton.setEnabled((null != enableReadDocxParametersButtons ? !enableReadDocxParametersButtons : !isBotDocxHandler)
                        && botTask.getType() != BotTaskType.PARAMETERIZED && selection.getFirstElement() != null);
                readDocxParametersButton.setEnabled(enableReadDocxButton != null ? enableReadDocxButton : isBotDocxHandler);
            }
        });
    }

    private void setTableInput(String groupType) {
        TableViewer confTableViewer = getParamTableViewer(groupType);
        List<ParamDef> paramDefs = new ArrayList<ParamDef>();
        for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
            if (groupType.equals(group.getName())) {
                paramDefs.addAll(group.getParameters());
            }
        }
        List<String[]> input = new ArrayList<String[]>(paramDefs.size());
        for (ParamDef paramDef : paramDefs) {
            String typeLabel = "";
            if (paramDef.getFormatFilters().size() > 0) {
                String type = paramDef.getFormatFilters().get(0);
                typeLabel = VariableFormatRegistry.getInstance().getFilterLabel(type);
            }
            String required = Localization.getString(paramDef.isOptional() ? "no" : "yes");
            String useVariable = Localization.getString(paramDef.isUseVariable() ? "yes" : "no");
            input.add(new String[] { paramDef.getName(), typeLabel, required, useVariable });
        }
        confTableViewer.setInput(input);
    }

    private TableViewer getParamTableViewer(String parameterType) {
        if (ParamDefGroup.NAME_INPUT.equals(parameterType)) {
            return inputParamTableViewer;
        } else {
            return outputParamTableViewer;
        }
    }

    private static class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            String[] data = (String[]) element;
            return data[index];
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private void populateFields() {
        handlerText.setText(botTask.getDelegationClassName());
        if (botTask.getType() != BotTaskType.PARAMETERIZED) {
            if (null != configurationText) {
                configurationText.setText(botTask.getDelegationConfiguration());
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (PropertyNames.PROPERTY_DIRTY.equals(evt.getPropertyName())) {
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {
        EditorUtils.closeEditorIfRequired(event, botTaskFile, this);
    }
}
