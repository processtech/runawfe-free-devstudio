package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.Activator;
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
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
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
    private DocxDialogEnhancementMode docxDialogEnhancementModeInput, docxDialogEnhancementModeOutput;

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

        if (isBotDocxHandlerEnhancement()) {
            initEmbeddedFileNameTimer();
        }
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

    public static void refreshAllBotTaskEditors() {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        for (IEditorReference ref : page.getEditorReferences()) {
            IEditorPart editor = ref.getEditor(true);
            if (editor instanceof BotTaskEditor) {
                ((BotTaskEditor) editor).recreateView();
            }
        }
    }

    public void recreateView() {
        rebuildView(editorComposite);
    }

    @Override
    public void createPartControl(Composite parent) {
        editorComposite = new Composite(parent, SWT.NONE);
        editorComposite.setLayout(new GridLayout());
        editorComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        rebuildView(editorComposite);
    }

    private void rebuildView(Composite composite) {
        configurationText = null;
        addParameterButton = null;
        editParameterButton = null;
        deleteParameterButton = null;
        readDocxParametersButton = null;

        for (Control control : composite.getChildren()) {
            control.dispose();
        }

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
            boolean isBotDocxHandlerEnhancement = isBotDocxHandlerEnhancement();
            if (!isBotDocxHandlerEnhancement) {
                createConfigurationFields(innerComposite);
            }
            createConfigurationArea(innerComposite);
            if (isBotDocxHandlerEnhancement) {
                createButtonParametersDisabler(composite, innerComposite);
            }

            scrolledComposite.setMinSize(SWT.DEFAULT, 700);
            scrolledComposite.setContent(innerComposite);
        }
        populateFields();
        composite.layout(true, true);
    }

    void initEmbeddedFileNameTimer() {
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

            }
        };

        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, 300);
    }

    void killEmbeddedFileNameTimer() {
        // timer.cancel();
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
            createButtonParametersDisabler(mainComposite, parent);
        }

        if (botTask.getType() == BotTaskType.EXTENDED) {
            Object docxModel = null;
            if (isBotDocxHandlerEnhancement) {
                Composite dynaComposite = new Composite(parent, SWT.NONE);
                dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                dynaComposite.setLayout(new GridLayout(2, true));
                DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                docxModel = provider.showEmbeddedConfigurationDialog(dynaComposite, botTask,
                        docxDialogEnhancementModeInput = new DocxDialogEnhancementMode(DocxDialogEnhancementMode.DOCX_SHOW_INPUT) {
                            @Override
                            public void reloadBotTaskEditorXmlFromModel(String newConfiguration, String embeddedFileName,
                                    Boolean enableReadDocxButton, Boolean enableDocxMode) {
                                reloadDialogXmlFromModel(newConfiguration, embeddedFileName, enableReadDocxButton, enableDocxMode);
                            }

                            @Override
                            public void invoke(long flags) {
                                if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_RELOAD_FROM_TEMPLATE)) {
                                    try {
                                        updateFromTemplate();
                                        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_MAKE_DIRTY)) {
                                            setDirty(true);
                                        }
                                    } catch (IOException | CoreException e) {
                                        e.printStackTrace();
                                    }
                                } else if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_INPUT_VARIABLE_MODE)) {
                                    if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_CREATE_VARIABLE)) {
                                        createFileParam(ParamDefGroup.NAME_INPUT, DocxDialogEnhancementMode.getInputFileParamName());
                                        docxDialogEnhancementModeInput.invokeObserver(flags);
                                    } else if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_DELETE_VARIABLE)) {
                                        deleteFileParam(ParamDefGroup.NAME_INPUT, DocxDialogEnhancementMode.getInputFileParamName());
                                    }
                                } else if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_MAKE_DIRTY)) {
                                    setDirty(true);
                                }
                            }
                        });
                createButtonUpdateFromTemplate(dynaComposite);
            }

            createConfTableViewer(parent, ParamDefGroup.NAME_INPUT);

            if (isBotDocxHandlerEnhancement) {
                DelegableProvider provider = HandlerRegistry.getProvider(botTask.getDelegationClassName());
                docxDialogEnhancementModeOutput = new DocxDialogEnhancementMode(DocxDialogEnhancementMode.DOCX_SHOW_OUTPUT) {

                    @Override
                    public void reloadBotTaskEditorXmlFromModel(String newConfiguration, String embeddedFileName, Boolean enableReadDocxButton,
                            Boolean enableDocxMode) {
                        reloadDialogXmlFromModel(newConfiguration, embeddedFileName, enableReadDocxButton, enableDocxMode);
                    }

                    @Override
                    public void invoke(long flags) {
                        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE)) {
                            if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_CREATE_VARIABLE)) {
                                createFileParam(ParamDefGroup.NAME_OUTPUT, DocxDialogEnhancementMode.getOutputFileParamName());
                                docxDialogEnhancementModeOutput.invokeObserver(flags);
                            } else if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_DELETE_VARIABLE)) {
                                deleteFileParam(ParamDefGroup.NAME_OUTPUT, DocxDialogEnhancementMode.getOutputFileParamName());
                            }
                        }
                    }

                };
                docxDialogEnhancementModeOutput.docxModel = docxModel;
                provider.showEmbeddedConfigurationDialog(parent, botTask, docxDialogEnhancementModeOutput);
            } else {
                createConfTableViewer(parent, ParamDefGroup.NAME_OUTPUT);
            }
        }
    }

    private boolean deleteFileParam(String groupName, String fileParamName) {
        for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
            if (groupName.equals(group.getName())) {
                List<ParamDef> params = group.getParameters();
                ListIterator<ParamDef> paramsIterator = params.listIterator();
                while (paramsIterator.hasNext()) {
                    ParamDef pd = paramsIterator.next();
                    String paramName = pd.getName();
                    if (paramName.compareTo(fileParamName) == 0) {
                        if (pd.getFormatFilters().size() > 0
                                && pd.getFormatFilters().get(0).compareTo(DocxDialogEnhancementMode.FILE_VARIABLE_FORMAT) == 0) {
                            paramsIterator.remove();
                            setDirty(true);
                            setTableInput(ParamDefGroup.NAME_INPUT);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean createFileParam(String groupName, String fileParamName) {
        for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
            if (groupName.equals(group.getName())) {
                boolean wasFileParam = false;
                List<ParamDef> params = group.getParameters();
                ListIterator<ParamDef> paramsIterator = params.listIterator();
                while (paramsIterator.hasNext()) {
                    ParamDef pd = paramsIterator.next();
                    String paramName = pd.getName();
                    if (paramName.compareTo(fileParamName) == 0) {
                        if (pd.getFormatFilters().size() > 0
                                && pd.getFormatFilters().get(0).compareTo(DocxDialogEnhancementMode.FILE_VARIABLE_FORMAT) == 0) {
                            wasFileParam = true;
                            continue;
                        }
                    }
                }

                if (!wasFileParam) {
                    ParamDef pd = new ParamDef(fileParamName, fileParamName);
                    List<String> formats = pd.getFormatFilters();
                    formats.add(DocxDialogEnhancementMode.FILE_VARIABLE_FORMAT);
                    params.add(pd);
                    setDirty(true);
                    setTableInput(ParamDefGroup.NAME_INPUT);
                    return true;
                }
                break;
            }
        }
        return false;
    }

    private void createButtonUpdateFromTemplate(Composite buttonArea) {
        if (!isBotDocxHandlerEnhancement()) {
            return;
        }
        readDocxParametersButton = new Button(buttonArea, SWT.NONE);
        readDocxParametersButton.setText(Localization.getString("button.read.docx"));
        readDocxParametersButton.setVisible(enableReadDocxParametersButtons != null ? enableReadDocxParametersButtons : true);
        readDocxParametersButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateFromTemplate();
            }
        });
    }

    private void updateFromTemplate() throws IOException, CoreException {
        if (embeddedFileName == null || embeddedFileName.isEmpty() || !EmbeddedFileUtils.isBotTaskFile(embeddedFileName)) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.badEmbeddedDocxFile"));
            return;
        }
        IFile file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getBotTaskFileName(embeddedFileName));
        if (null == file || !file.exists()) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetFile"));
            return;
        }
        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetInputStream"));
                return;
            }
            Map<String, Integer> variablesMap = DocxDialogEnhancementMode.getVariableNamesFromDocxTemplate(inputStream);
            for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                if (ParamDefGroup.NAME_INPUT.equals(group.getName())) {
                    String inputFileParamName = DocxDialogEnhancementMode.getInputFileParamName();
                    List<ParamDef> params = group.getParameters();
                    ListIterator<ParamDef> paramsIterator = params.listIterator();
                    while (paramsIterator.hasNext()) {
                        ParamDef pd = paramsIterator.next();
                        String paramName = pd.getName();
                        if (paramName.equals(inputFileParamName)) {
                            if (pd.getFormatFilters().size() > 0
                                    && pd.getFormatFilters().get(0).compareTo(DocxDialogEnhancementMode.FILE_VARIABLE_FORMAT) == 0) {
                                continue;
                            }
                        }
                        if (!variablesMap.containsKey(paramName)) {
                            paramsIterator.remove();
                            setDirty(true);
                        }
                    }
                    for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                        ListIterator<ParamDef> paramsIter = params.listIterator();
                        boolean exists = false;
                        while (paramsIter.hasNext()) {
                            if (paramsIter.next().getName().compareTo(entry.getKey()) == 0) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            ParamDef pd = new ParamDef(entry.getKey(), entry.getKey());
                            List<String> formats = pd.getFormatFilters();
                            formats.add("java.lang.Object");
                            params.add(pd);
                            setDirty(true);
                        }
                    }
                    break;
                }
            }
            setTableInput(ParamDefGroup.NAME_INPUT);
            docxDialogEnhancementModeOutput.invokeObserver(DialogEnhancementMode.DOCX_OUTPUT_VARIABLE_MODE);
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
                readDocxParametersButton.setVisible(enableReadDocxButton);
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
            if (null != docxDialogEnhancementModeInput) {
                docxDialogEnhancementModeInput.enableDocxMode = enableDocxMode;
            }
            if (null != docxDialogEnhancementModeOutput) {
                docxDialogEnhancementModeOutput.enableDocxMode = enableDocxMode;
            }
        }

    }

    private void createButtonParametersDisabler(final Composite mainComposite, final Composite innerComposite) {
        if (!Activator.getPrefBoolean(PrefConstants.P_ENABLE_USE_BOT_CONFIG_WITHOUT_PARAMETERS_OPTION)) {
            return;
        }

        Composite dynaComposite = new Composite(innerComposite, SWT.NONE);
        dynaComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dynaComposite.setLayout(new GridLayout(3, false));

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
        return null != botTask && DialogEnhancement.isOn()
                && 0 == botTask.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID);
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
                String newConfiguration = provider.showConfigurationDialog(botTask, null);
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
        if (!Activator.getPrefBoolean(PrefConstants.P_SHOW_XML_BOT_CONFIG)) {
            return;
        }
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

        Button addParameterButtonLocal = new Button(buttonArea, SWT.NONE);
        if (isBotDocxHandler) {
            addParameterButton = addParameterButtonLocal;
        }
        addParameterButtonLocal.setText(Localization.getString("button.add"));
        addParameterButtonLocal.setEnabled(!isBotDocxHandler && botTask.getType() != BotTaskType.PARAMETERIZED);
        addParameterButtonLocal.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (parameterType.equals(group.getName())) {
                        BotTaskParamDefWizard wizard = new BotTaskParamDefWizard(group, null, new DocxDialogEnhancementMode(0));
                        CompactWizardDialog dialog = new CompactWizardDialog(wizard);
                        if (dialog.open() == Window.OK) {
                            setTableInput(parameterType);
                            setDirty(true);
                        }
                    }
                }
            }
        });

        Button editParameterButtonLocal = new Button(buttonArea, SWT.NONE);
        if (isBotDocxHandler) {
            editParameterButton = editParameterButtonLocal;
        }
        editParameterButtonLocal.setText(Localization.getString("button.edit"));
        editParameterButtonLocal.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED
                && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        editParameterButtonLocal.addSelectionListener(new LoggingSelectionAdapter() {

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
                                BotTaskParamDefWizard wizard = new BotTaskParamDefWizard(group, paramDef, docxDialogEnhancementModeInput);
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

        Button deleteParameterButtonLocal = new Button(buttonArea, SWT.NONE);
        if (isBotDocxHandler) {
            deleteParameterButton = deleteParameterButtonLocal;
        }
        deleteParameterButtonLocal.setText(Localization.getString("button.delete"));
        deleteParameterButtonLocal.setEnabled((!DialogEnhancement.isOn() || !isBotDocxHandler) && botTask.getType() != BotTaskType.PARAMETERIZED
                && ((IStructuredSelection) getParamTableViewer(parameterType).getSelection()).getFirstElement() != null);
        deleteParameterButtonLocal.addSelectionListener(new LoggingSelectionAdapter() {
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

        confTableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                addParameterButtonLocal.setEnabled(null != enableReadDocxParametersButtons ? !enableReadDocxParametersButtons : !isBotDocxHandler);
                editParameterButtonLocal.setEnabled(botTask.getType() != BotTaskType.PARAMETERIZED && selection.getFirstElement() != null);
                deleteParameterButtonLocal.setEnabled((!DialogEnhancement.isOn()
                        || (null != enableReadDocxParametersButtons ? !enableReadDocxParametersButtons : !isBotDocxHandler))
                        && botTask.getType() != BotTaskType.PARAMETERIZED && selection.getFirstElement() != null);
                if (null != readDocxParametersButton) {
                    readDocxParametersButton.setVisible(enableReadDocxButton != null ? enableReadDocxButton : isBotDocxHandler);
                }
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
