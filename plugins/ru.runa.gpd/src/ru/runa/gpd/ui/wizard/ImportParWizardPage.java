package ru.runa.gpd.ui.wizard;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.dom4j.Document;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.BpmnSerializer;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.GlobalSectionDefinition;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.sync.WfeServerConnectorSynchronizationCallback;
import ru.runa.gpd.sync.WfeServerProcessDefinitionImporter;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.gpd.util.files.ParFileImporter;
import ru.runa.gpd.util.files.ProcessDefinitionImportInfo;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.user.Actor;

public class ImportParWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedParsText;
    private Button selectParsButton;
    private Button importFromServerButton;
    private WfeServerConnectorComposite serverConnectorComposite;
    private TreeViewer serverDefinitionViewer;
    private Text serverDefinitionFilter;
    private Button clearFilterButton;
    private String selectedDirFileName;
    private String[] selectedFileNames;
    private int importInfosSize;
    private Exception exceptionFromThreads;

    public ImportParWizardPage(IStructuredSelection selection) {
        super(ImportParWizardPage.class, selection);
        setTitle(Localization.getString("ImportParWizardPage.page.title"));
    }

    protected ImportParWizardPage(Class<? extends ImportParWizardPage> clazz, IStructuredSelection selection) {
        super(clazz, selection);
    }

    @Override
    protected IContainer getInitialSelection(IStructuredSelection selection) {
        return (IContainer) IOUtils.getProcessSelectionResource(selection);
    }

    public String fileExtension() {
        return ".par";
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        createProjectsGroup(sashForm, IOUtils.getAllProcessContainers(), new LabelProvider() {
            @Override
            public String getText(Object element) {
                return IOUtils.getProcessContainerName((IContainer) element);
            }
        });
        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        importFromFileButton = new Button(importGroup, SWT.RADIO);
        importFromFileButton.setText(Localization.getString("button.importFromFile"));
        importFromFileButton.setSelection(true);
        importFromFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onImportModeChanged();
            }
        });
        fileSelectionArea = new Composite(importGroup, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionData.heightHint = 30;
        fileSelectionArea.setLayoutData(fileSelectionData);
        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 2;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);
        selectedParsText = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedParsText.setLayoutData(gridData);
        selectParsButton = new Button(fileSelectionArea, SWT.PUSH);
        selectParsButton.setText(Localization.getString("button.choose"));
        selectParsButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*" + fileExtension() });
                if (dialog.open() != null) {
                    selectedDirFileName = dialog.getFilterPath();
                    selectedFileNames = dialog.getFileNames();
                    String text = "";
                    for (String fileName : selectedFileNames) {
                        text += fileName + "\n";
                    }
                    selectedParsText.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("button.importFromServer"));
        serverConnectorComposite = new WfeServerConnectorComposite(importGroup, WfeServerProcessDefinitionImporter.getInstance(),
                new WfeServerConnectorSynchronizationCallback() {

                    @Override
                    public void onCompleted() {
                        updateServerDefinitionViewer(WfeServerProcessDefinitionImporter.getInstance().getData());
                    }

                    @Override
                    public void onFailed() {
                        updateServerDefinitionViewer(null);
                    }

                });
        createServerDefinitionsGroup(importGroup);
        setControl(pageControl);
        onImportModeChanged();
    }

    private void onImportModeChanged() {
        boolean fromFile = importFromFileButton.getSelection();
        selectedParsText.setEnabled(fromFile);
        selectParsButton.setEnabled(fromFile);
        serverConnectorComposite.setEnabled(!fromFile);
        serverDefinitionViewer.getControl().setEnabled(!fromFile);
        serverDefinitionFilter.setEnabled(!fromFile);
        clearFilterButton.setEnabled(!fromFile);
        if (fromFile) {
            updateServerDefinitionViewer(null);
        } else {
            updateServerDefinitionViewer(WfeServerProcessDefinitionImporter.getInstance().getData());
        }
    }

    private void updateServerDefinitionViewer(List<WfDefinition> definitions) {
        serverDefinitionFilter.setText("");
        if (definitions != null) {
            DefinitionTreeNode treeDefinitions = createTree(definitions);
            serverDefinitionViewer.setInput(treeDefinitions);
        } else {
            serverDefinitionViewer.setInput(new Object());
        }
        serverDefinitionViewer.refresh(true);
    }

    private void createServerDefinitionsGroup(Composite parent) {
        Composite filterArea = new Composite(parent, SWT.NONE);
        filterArea.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL));
        GridLayout filterLayout = new GridLayout();
        filterLayout.numColumns = 2;
        filterLayout.makeColumnsEqualWidth = false;
        filterLayout.marginWidth = 0;
        filterLayout.marginHeight = 0;
        filterArea.setLayout(filterLayout);
        serverDefinitionFilter = new Text(filterArea, SWT.SINGLE | SWT.BORDER);
        serverDefinitionFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        serverDefinitionFilter.setMessage(Localization.getString("text.message.filter"));
        serverDefinitionFilter.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                serverDefinitionViewer.refresh();
            }
        });
        clearFilterButton = new Button(filterArea, SWT.PUSH);
        clearFilterButton.setToolTipText(Localization.getString("button.clear"));
        clearFilterButton.setImage(WorkbenchImages.getImage(ISharedImages.IMG_ETOOL_CLEAR));
        clearFilterButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        clearFilterButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                serverDefinitionFilter.setText("");
            }
        });

        serverDefinitionViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 300;
        serverDefinitionViewer.getControl().setLayoutData(gridData);
        serverDefinitionViewer.setContentProvider(new ViewContentProvider());
        serverDefinitionViewer.setLabelProvider(new ViewLabelProvider());
        serverDefinitionViewer.setInput(new Object());
        serverDefinitionViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                String searchText = serverDefinitionFilter.getText();
                if (searchText == null || searchText.trim().length() == 0) {
                    return true;
                }

                DefinitionTreeNode node = (DefinitionTreeNode) element;
                if (node.definition == null && node.isGroupNode()) {
                    // This is the node. Show nodes with at least one matching child definition
                    return matchAtLeastOneSub(node.children, searchText);
                }
                if (matchNode(node, searchText)) {
                    return true;

                }
                return false;
            }
        });
    }

    private boolean matchAtLeastOneSub(List<DefinitionTreeNode> source, String searchText) {
        if (source == null || source.size() == 0) {
            return false;
        }

        for (DefinitionTreeNode node : source) {
            if (matchNode(node, searchText)) {
                return true;
            }
            if (node.children.size() > 0) {
                boolean bSub = matchAtLeastOneSub(node.children, searchText);
                if (bSub) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchNode(DefinitionTreeNode node, String searchText) {
        return node.definition != null && node.definition.getName().toLowerCase().contains(searchText.trim().toLowerCase());
    }

    public void setImportInfosSize(int size) {
        this.importInfosSize = size;
    }

    int getImportInfosSize() {
        return this.importInfosSize;
    }

    public boolean performFinish() {
        List<ProcessDefinitionImportInfo> importInfos = Lists.newArrayList();
        try {
            IContainer selectedProject = getSelectedProject();
            if (selectedProject == null) {
                return false;
            }
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("error.selectValidFile"));
                }
                for (int i = 0; i < selectedFileNames.length; i++) {
                    String definitionName = selectedFileNames[i].substring(0, selectedFileNames[i].length() - fileExtension().length());
                    String fileName = selectedDirFileName + File.separator + selectedFileNames[i];
                    importInfos.add(new ProcessDefinitionImportInfo(definitionName, "", new FileInputStream(fileName)));
                }
            } else {
                TreeItem[] selectionInTree = serverDefinitionViewer.getTree().getSelection();
                DefinitionTreeNode[] definitionTreeNodes = new DefinitionTreeNode[selectionInTree.length];
                int importInfosSize = 0;
                for (int i = 0; i < selectionInTree.length; i++) {
                    definitionTreeNodes[i] = (DefinitionTreeNode) selectionInTree[i].getData();
                    importInfosSize += definitionTreeNodes[i].getSize();
                }
                this.setImportInfosSize(importInfosSize);
                ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(Display.getCurrent().getActiveShell());
                IRunnableWithProgress importRunnable = new IRunnableWithProgress() {

                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        int importInfosSize = getImportInfosSize();
                        SubMonitor subMonitor = SubMonitor.convert(monitor, importInfosSize);
                        try {
                            exceptionFromThreads = null;
                            ExecutorService executorService = Executors.newSingleThreadExecutor();
                            ExceptionListener listener = new ExceptionListener() {
                                @Override
                                public void exceptionThrown(Exception e) {
                                    exceptionFromThreads = e;
                                }
                            };
                            List<Future<ProcessDefinitionImportInfo>> importInfosFuture = new ArrayList<>(importInfosSize);
                            for (int i = 0; i < selectionInTree.length; i++) {
                                DefinitionTreeNode treeNode = definitionTreeNodes[i];
                                importInfosFuture.addAll(treeNode.toRecursiveImportInfo("", executorService, listener));
                            }
                            int currentProgress = 0;
                            while (importInfosFuture.stream().anyMatch(future -> !future.isDone())) {
                                if (exceptionFromThreads != null) {
                                    throw exceptionFromThreads;
                                }
                                if (monitor.isCanceled()) {
                                    executorService.shutdownNow();
                                    throw new OperationCanceledException();
                                }
                                int currentProgressNew = (int) importInfosFuture.stream().filter(Future::isDone).count();
                                subMonitor.split(currentProgressNew - currentProgress);
                                currentProgress = currentProgressNew;
                                Thread.sleep(100);
                            }
                            if (exceptionFromThreads != null) {
                                throw exceptionFromThreads;
                            } else {
                                for (Future<ProcessDefinitionImportInfo> future : importInfosFuture) {
                                    importInfos.add(future.get());
                                }
                            }
                        } catch (OperationCanceledException e) {
                            throw new InterruptedException("import was cancelled by pressing cancel button");
                        } catch (Exception e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                };
                monitorDialog.run(true, true, importRunnable);
            }
            if (importInfos.isEmpty()) {
                setErrorMessage(Localization.getString("ImportParWizardPage.error.selectValidDefinition"));
                return false;
            }
            final ParFileImporter importer = new ParFileImporter(selectedProject);
            for (ProcessDefinitionImportInfo importInfo : importInfos) {
                if (importer.importFile(importInfo) == null) {
                    setErrorMessage(Localization.getString("ImportParWizardPage.error.processWithSameNameExists", importInfo.getPath()));
                    return false;
                } else if (importer.getDefinition() != null) {
                    ProcessDefinition processDefinition = importer.getDefinition();
                    if (processDefinition.isUsingGlobalVars()) {
                        GlobalSectionDefinition globalDefinition = ProcessCache.getGlobalProcessDefinition(processDefinition);
                        if (globalDefinition == null) {
                            IPath globalPath = new Path(IPath.SEPARATOR + ".global");
                            IFolder folder = selectedProject.getFolder(globalPath);
                            folder.create(true, true, null);
                            IFile globalSectionFile = IOUtils.getProcessDefinitionFile(folder);
                            String processName = "Global";
                            Language language = processDefinition.getLanguage();
                            Map<String, String> properties = Maps.newHashMap();
                            if (language == Language.BPMN) {
                                properties.put(BpmnSerializer.SHOW_SWIMLANE, processDefinition.getSwimlaneDisplayMode().name());
                            }
                            Document document = language.getSerializer().getInitialProcessDefinitionDocument(processName, properties);
                            byte[] bytes = XmlUtil.writeXml(document);
                            globalSectionFile.create(new ByteArrayInputStream(bytes), true, null);
                            ProcessCache.newProcessDefinitionWasCreated(globalSectionFile);
                            globalDefinition = (GlobalSectionDefinition) ProcessCache.getProcessDefinition(globalSectionFile);
                            WorkspaceOperations.openGlobalSectionDefinition(globalSectionFile);
                        }
                        for (Swimlane swimlane : processDefinition.getGlobalSwimlanes()) {
                            globalDefinition.addSwimlane(swimlane.getCopyForGlobalPartition());
                        }
                        for (Variable variable : processDefinition.getGlobalVariables()) {
                            globalDefinition.addVariable(variable.getCopyForGlobalPartition());
                        }
                        for (VariableUserType type : processDefinition.getGlobalTypes()) {
                            globalDefinition.addVariableUserType(type.getCopyForGlobalPartition());
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            return false; // import was cancelled by pressing cancel button, not exit from wizard
        } catch (Exception exception) {
            PluginLogger.logErrorWithoutDialog("import par", exception);
            setErrorMessage(Throwables.getRootCause(exception).getMessage());
            return false;
        } finally {
            for (ProcessDefinitionImportInfo importInfo : importInfos) {
                try {
                    importInfo.close();
                } catch (IOException e) {
                }
            }
        }
        return true;
    }

    private DefinitionTreeNode createTree(List<WfDefinition> definitions) {
        DefinitionTreeNode rootTreeNode = new DefinitionTreeNode("", "", null, false, false);
        Collections.sort(definitions, new Comparator<WfDefinition>() {

            @Override
            public int compare(WfDefinition o1, WfDefinition o2) {
                String[] categories1 = o1.getCategories();
                String[] categories2 = o2.getCategories();
                if (categoryIsEmpty(categories1)) {
                    if (categoryIsEmpty(categories2)) {
                        return 0;
                    }
                    return 1;
                }
                if (categoryIsEmpty(categories2)) {
                    return -1;
                }
                return categories1[0].compareTo(categories2[0]);
            }

            private boolean categoryIsEmpty(String[] categories) {
                return categories == null || categories.length == 0 || Strings.isNullOrEmpty(categories[0]) || categories[0].trim().length() == 0;
            }
        });
        for (WfDefinition definition : definitions) {
            rootTreeNode.addElementToTree(rootTreeNode.path, definition.getCategories(), definition);
        }
        return rootTreeNode;
    }

    interface ExceptionListener {
        public void exceptionThrown(Exception e);
    }

    class ViewLabelProvider extends LabelProvider {

        @Override
        public String getText(Object obj) {
            DefinitionTreeNode treeNode = (DefinitionTreeNode) obj;
            return treeNode.getLabel();
        }

        @Override
        public Image getImage(Object obj) {
            DefinitionTreeNode definitionNode = (DefinitionTreeNode) obj;
            return definitionNode.groupNode ? SharedImages.getImage("icons/project.gif") : SharedImages.getImage("icons/process.gif");
        }

    }

    class ViewContentProvider implements IStructuredContentProvider, ITreeContentProvider {

        @Override
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Object[] getElements(Object parent) {
            return getChildren(parent);
        }

        @Override
        public Object getParent(Object child) {
            if (child instanceof DefinitionTreeNode) {
                return ((DefinitionTreeNode) child).getChildren();
            }
            return null;
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof DefinitionTreeNode) {
                return ((DefinitionTreeNode) parent).getChildren().toArray();
            }
            return new Object[0];
        }

        @Override
        public boolean hasChildren(Object parent) {
            if (parent instanceof DefinitionTreeNode) {
                return !((DefinitionTreeNode) parent).getChildren().isEmpty();
            }
            return false;
        }
    }

    static class DefinitionTreeNode {
        private final String path;
        private final String label;
        private final WfDefinition definition;
        private final boolean groupNode;
        private final boolean historyNode;
        private final List<DefinitionTreeNode> children = new ArrayList<DefinitionTreeNode>();

        public DefinitionTreeNode(String parentPath, String label, WfDefinition definition, boolean groupNode, boolean historyNode) {
            this.path = parentPath + File.separator + label;
            this.label = label;
            this.definition = definition;
            this.groupNode = groupNode;
            this.historyNode = historyNode;
        }

        public String getPath() {
            return path;
        }

        public WfDefinition getDefinition() {
            return definition;
        }

        private String getLabel() {
            return label;
        }

        private boolean isHistoryNode() {
            return historyNode;
        }

        private boolean isGroupNode() {
            return groupNode;
        }

        private void addElementToTree(String path, String[] categories, WfDefinition definition) {
            if (categories.length == 0) {
                DefinitionTreeNode leafNode = new DefinitionTreeNode(path, definition.getName(), definition, false, false);
                if (WfeServerConnector.getInstance().getSettings().isLoadProcessDefinitionsHistory()) {
                    String label = Localization.getString("ImportParWizardPage.page.oldDefinitionVersions");
                    DefinitionTreeNode historyGroup = new DefinitionTreeNode(leafNode.path, label, definition, true, true);
                    leafNode.getChildren().add(historyGroup);
                }
                children.add(leafNode);
                return;
            }
            DefinitionTreeNode groupTreeNode = new DefinitionTreeNode(path, categories[0], null, true, false);
            int index = children.indexOf(groupTreeNode);
            if (index == -1) {
                // child in new group
                children.add(groupTreeNode);
            } else {
                // child in existed group
                groupTreeNode = children.get(index);
            }
            String[] newCategories = Arrays.copyOfRange(categories, 1, categories.length);
            groupTreeNode.addElementToTree(groupTreeNode.path, newCategories, definition);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DefinitionTreeNode) {
                return path.equals(((DefinitionTreeNode) obj).path);
            }
            return super.equals(obj);
        }

        @Override
        public String toString() {
            return label;
        }

        private String getHistoryDefinitionLabel(WfDefinition definition) {
            String dateString = "-";
            String actorString = "-";
            String sign = definition.getUpdateDate() != null ? "*" : "";
            Date date = definition.getUpdateDate();
            Actor actor = definition.getUpdateActor();
            if (date == null) {
                date = definition.getCreateDate();
                actor = definition.getCreateActor();
            }
            if (date != null) {
                dateString = CalendarUtil.formatDateTime(date);
            }
            if (actor != null) {
                actorString = actor.getName();
            }
            return String.format("%d%s (%s) - %s", definition.getVersion(), sign, dateString, actorString);
        }

        private List<DefinitionTreeNode> getChildren() {
            if (groupNode && historyNode && children.isEmpty()) {
                Shell shell = Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : null;
                final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
                monitorDialog.setCancelable(true);
                final IRunnableWithProgress runnable = new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                        try {
                            monitor.beginTask(NLS.bind(Localization.getString("task.LoadHistoryData"), definition.getName()), 1);
                            List<WfDefinition> list = WfeServerConnector.getInstance().getProcessDefinitionHistory(definition);
                            if (list.isEmpty()) {
                                String label = Localization.getString("ImportParWizardPage.page.oldDefinitionVersions.empty");
                                DefinitionTreeNode historyDefinitionNode = new DefinitionTreeNode(path, label, null, false, true);
                                children.add(historyDefinitionNode);
                            }
                            for (WfDefinition definition : list) {
                                String label = getHistoryDefinitionLabel(definition);
                                DefinitionTreeNode historyDefinitionNode = new DefinitionTreeNode(path, label, definition, false, true);
                                children.add(historyDefinitionNode);
                            }
                            monitor.done();
                        } catch (Exception e) {
                            PluginLogger.logErrorWithoutDialog("error.Synchronize", e);
                            throw new InvocationTargetException(e);
                        } finally {
                            monitor.done();
                        }
                    }
                };
                try {
                    monitorDialog.run(true, false, runnable);
                } catch (InvocationTargetException ex) {
                    Dialogs.error(Localization.getString("error.Synchronize"), ex.getTargetException());
                } catch (InterruptedException consumed) {
                }
            }
            return children;
        }

        private List<Future<ProcessDefinitionImportInfo>> toRecursiveImportInfo(String importPath, ExecutorService executorService,
                ExceptionListener listener) throws Exception {
            List<Future<ProcessDefinitionImportInfo>> result = Lists.newArrayList();
            if (isGroupNode() && isHistoryNode()) {
                return result;
            }
            if (isGroupNode()) {
                for (DefinitionTreeNode currentNode : children) {
                    if (currentNode != null) {
                        result.addAll(currentNode.toRecursiveImportInfo(importPath + File.separator + label, executorService, listener));
                    }
                }
            } else if (definition != null) {
                Callable<ProcessDefinitionImportInfo> toImportInfoCallable = new Callable<ProcessDefinitionImportInfo>() {

                    @Override
                    public ProcessDefinitionImportInfo call() {
                        try {
                            return toImportInfo(importPath);
                        } catch (Exception e) {
                            listener.exceptionThrown(e);
                            return null;
                        }
                    }
                };
                Future<ProcessDefinitionImportInfo> future = executorService.submit(toImportInfoCallable);
                result.add(future);
            }
            return result;
        }

        private ProcessDefinitionImportInfo toImportInfo(String importPath) throws Exception {
            byte[] par = WfeServerProcessDefinitionImporter.getInstance().loadPar(definition);
            return new ProcessDefinitionImportInfo(definition.getName(), importPath, new ByteArrayInputStream(par));
        }

        int getSize() {
            int size = 0;
            if (isGroupNode()) {
                for (DefinitionTreeNode currentNode : children) {
                    if (currentNode != null) {
                        size += currentNode.getSize();
                    }
                }
            } else if (definition != null) {
                return 1;
            }
            return size;
        }
    }
}
