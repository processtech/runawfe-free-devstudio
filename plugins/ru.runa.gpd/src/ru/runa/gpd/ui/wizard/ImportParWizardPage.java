package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.wfe.ConnectorCallback;
import ru.runa.gpd.wfe.WFEServerProcessDefinitionImporter;
import ru.runa.wfe.definition.dto.WfDefinition;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class ImportParWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedParsLabel;
    private Button selectParsButton;
    private Button importFromServerButton;
    private TreeViewer serverDefinitionViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    public ImportParWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        setTitle(Localization.getString("ImportParWizardPage.page.title"));
        setDescription(Localization.getString("ImportParWizardPage.page.description"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        createProjectsGroup(sashForm);
        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        importFromFileButton = new Button(importGroup, SWT.RADIO);
        importFromFileButton.setText(Localization.getString("ImportParWizardPage.page.importFromFileButton"));
        importFromFileButton.setSelection(true);
        importFromFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setImportMode();
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
        selectedParsLabel = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedParsLabel.setLayoutData(gridData);
        selectParsButton = new Button(fileSelectionArea, SWT.PUSH);
        selectParsButton.setText(Localization.getString("button.choose"));
        selectParsButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*.par" });
                if (dialog.open() != null) {
                    selectedDirFileName = dialog.getFilterPath();
                    selectedFileNames = dialog.getFileNames();
                    String text = "";
                    for (String fileName : selectedFileNames) {
                        text += fileName + "\n";
                    }
                    selectedParsLabel.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("ImportParWizardPage.page.importFromServerButton"));
        SyncUIHelper.createHeader(importGroup, WFEServerProcessDefinitionImporter.getInstance(), WFEConnectionPreferencePage.class,
                new ConnectorCallback() {

                    @Override
                    public void onSynchronizationFailed(Exception e) {
                        Dialogs.error(Localization.getString("error.Synchronize"), e);
                    }

                    @Override
                    public void onSynchronizationCompleted() {
                    	Map<WfDefinition, List<WfDefinition>> definitions = WFEServerProcessDefinitionImporter.getInstance().loadCachedData();
                        // TODO:if there are exist at least one group

                        TreeObject treeObject = createTree(getWfDefinitionsByType(definitions));
                        serverDefinitionViewer.setInput(treeObject);
                        serverDefinitionViewer.refresh(true);
                    }
                });
        createServerDefinitionsGroup(importGroup);
        setControl(pageControl);
    }

    private void setImportMode() {
        boolean fromFile = importFromFileButton.getSelection();
        selectParsButton.setEnabled(fromFile);
        if (fromFile) {
            serverDefinitionViewer.setInput(new Object());
        } else {
            if (WFEServerProcessDefinitionImporter.getInstance().isConfigured()) {
                if (!WFEServerProcessDefinitionImporter.getInstance().hasCachedData()) {
                    long start = System.currentTimeMillis();
                    WFEServerProcessDefinitionImporter.getInstance().synchronize();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo("def sync [sec]: " + ((end - start) / 1000));
                }
                Map<WfDefinition, List<WfDefinition>> definitions = WFEServerProcessDefinitionImporter.getInstance().loadCachedData();
                // TODO:if there are exist at least one group

                TreeObject treeObject = createTree(getWfDefinitionsByType(definitions));
                serverDefinitionViewer.setInput(treeObject);
                serverDefinitionViewer.refresh(true);
            }
        }
    }

    private void createServerDefinitionsGroup(Composite parent) {
        serverDefinitionViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        serverDefinitionViewer.getControl().setLayoutData(gridData);
        serverDefinitionViewer.setContentProvider(new ViewContentProvider());
        serverDefinitionViewer.setLabelProvider(new ViewLabelProvider());
        serverDefinitionViewer.setInput(new Object());
    }

    public boolean performFinish() {
        InputStream[] parInputStreams = null;
        try {
            IContainer container = getSelectedContainer();
            String[] processNames;
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidPar"));
                }
                processNames = new String[selectedFileNames.length];
                parInputStreams = new InputStream[selectedFileNames.length];
                for (int i = 0; i < selectedFileNames.length; i++) {
                    processNames[i] = selectedFileNames[i].substring(0, selectedFileNames[i].length() - 4);
                    String fileName = selectedDirFileName + File.separator + selectedFileNames[i];
                    parInputStreams[i] = new FileInputStream(fileName);
                }
            } else {
                List<?> selections = ((IStructuredSelection) serverDefinitionViewer.getSelection()).toList();
                List<WfDefinition> defSelections = Lists.newArrayList();
                for (Object object : selections) {
                    if (object instanceof WfDefinition) {
                        defSelections.add((WfDefinition) object);
                    }
                }
                if (defSelections.isEmpty()) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidDefinition"));
                }
                processNames = new String[defSelections.size()];
                parInputStreams = new InputStream[defSelections.size()];
                for (int i = 0; i < processNames.length; i++) {
                    WfDefinition stub = defSelections.get(i);
                    processNames[i] = stub.getName();
                    byte[] par = WFEServerProcessDefinitionImporter.getInstance().loadPar(stub);
                    parInputStreams[i] = new ByteArrayInputStream(par);
                }
            }
            for (int i = 0; i < processNames.length; i++) {
                String processName = processNames[i];
                IFolder processFolder = IOUtils.getProcessFolder(container, processName);
                if (processFolder.exists()) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.processWithSameNameExists"));
                }
                processFolder.create(true, true, null);
                IOUtils.extractArchiveToFolder(parInputStreams[i], processFolder);
                IFile definitionFile = IOUtils.getProcessDefinitionFile(processFolder);
                ProcessDefinition definition = ProcessCache.newProcessDefinitionWasCreated(definitionFile);
                if (definition != null && !Objects.equal(definition.getName(), processFolder.getName())) {
                    // if par name differs from definition name
                    IPath destination = IOUtils.getProcessFolder(container, definition.getName()).getFullPath();
                    processFolder.move(destination, true, false, null);
                    processFolder = IOUtils.getProcessFolder(container, definition.getName());
                    IFile movedDefinitionFile = IOUtils.getProcessDefinitionFile(processFolder);
                    ProcessCache.newProcessDefinitionWasCreated(movedDefinitionFile);
                    ProcessCache.invalidateProcessDefinition(definitionFile);
                }
            }
        } catch (Exception exception) {
            PluginLogger.logErrorWithoutDialog("import par", exception);
            setErrorMessage(Throwables.getRootCause(exception).getMessage());
            return false;
        } finally {
            if (parInputStreams != null) {
                for (InputStream inputStream : parInputStreams) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return true;
    }

    public static class DefinitionTreeContentProvider implements ITreeContentProvider {
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof HistoryRoot) {
                HistoryRoot historyRoot = (HistoryRoot) parentElement;
                List<WfDefinition> history = WFEServerProcessDefinitionImporter.getInstance().loadCachedData().get(historyRoot.definition);
                List<WfDefinition> result = Lists.newArrayList(history);
                result.remove(0);
                return result.toArray();
            }
            if (WFEServerProcessDefinitionImporter.getInstance().loadCachedData().containsKey(parentElement)) {
                return new Object[] { new HistoryRoot((WfDefinition) parentElement) };
            }
            return new Object[0];
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            if (element instanceof HistoryRoot) {
                return true;
            }
            List<WfDefinition> history = WFEServerProcessDefinitionImporter.getInstance().loadCachedData().get(element);
            return (history != null && history.size() > 1);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Map) {
                ArrayList<WfDefinition> arrayList = new ArrayList<WfDefinition>();
                arrayList.addAll(((Map<WfDefinition, List<WfDefinition>>) inputElement).keySet());
                Collections.sort(arrayList);
                return arrayList.toArray(new WfDefinition[arrayList.size()]);
            }
            return new Object[0];
        }

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    public static class HistoryRoot {
        private final WfDefinition definition;

        public HistoryRoot(WfDefinition stub) {
            this.definition = stub;
        }
    }

    public static class DefinitionLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof WfDefinition) {
                WfDefinition definition = (WfDefinition) element;
                if (WFEServerProcessDefinitionImporter.getInstance().loadCachedData().containsKey(definition)) {
                    return definition.getName();
                }
                return String.valueOf(definition.getVersion());
            }
            if (element instanceof HistoryRoot) {
                return Localization.getString("ImportParWizardPage.page.oldDefinitionVersions");
            }
            return super.getText(element);
        }
    }
    
    private Map<String, List<WfDefinition>> getWfDefinitionsByType(Map<WfDefinition, List<WfDefinition>> definitions) {
        Map grouppedDefinitionsMap = new HashMap<String, List<WfDefinition>>();
        for (Map.Entry<WfDefinition, List<WfDefinition>> entry : definitions.entrySet()) {
            WfDefinition definition = entry.getKey();
            String[] categories = definition.getCategories();

            for (String category : categories) {
                if (!grouppedDefinitionsMap.containsKey(category)) {
                    List<WfDefinition> newDefinitionlist = new ArrayList<WfDefinition>();
                    newDefinitionlist.add(definition);
                    grouppedDefinitionsMap.put(category, newDefinitionlist);
                } else {
                    List existedDefinitionlist = (List) grouppedDefinitionsMap.get(category);
                    existedDefinitionlist.add(definition);
                }
            }
        }
        return grouppedDefinitionsMap;
    }

    class ViewLabelProvider extends LabelProvider {

        @Override
        public String getText(Object obj) {
            return obj.toString();
        }

        @Override
        public Image getImage(Object obj) {
            String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
            if (obj instanceof ProcessType) {
                imageKey = ISharedImages.IMG_OBJ_FOLDER;
            }
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
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
            if (child instanceof TreeObject) {
                return ((TreeObject) child).getParent();
            }
            return null;
        }

        @Override
        public Object[] getChildren(Object parent) {
            if (parent instanceof ProcessType) {
                return ((ProcessType) parent).getChildren();
            }
            return new Object[0];
        }

        @Override
        public boolean hasChildren(Object parent) {
            if (parent instanceof ProcessType) {
                return ((ProcessType) parent).hasChildren();
            }
            return false;
        }
    }

    class TreeObject extends WfDefinition {
        private final String name;
        private ProcessType processType;
        private Long id;

        public TreeObject(Long id, String name) {
            this.name = name;
            this.id = id;
        }

        @Override
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setParent(ProcessType processType) {
            this.processType = processType;
        }

        public ProcessType getParent() {
            return processType;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    class ProcessType extends TreeObject {
        private final ArrayList children;

        public ProcessType(String name) {
            super(null, name);
            children = new ArrayList();
        }

        public void addChild(TreeObject child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeObject child) {
            children.remove(child);
            child.setParent(null);
        }

        public WfDefinition[] getChildren() {
            return (WfDefinition[]) children.toArray(new TreeObject[children.size()]);
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }
    }

    private TreeObject createTree(Map<String, List<WfDefinition>> definitions) {
        ProcessType root = new ProcessType("");
        ProcessType processType;
        for (Map.Entry<String, List<WfDefinition>> entry : definitions.entrySet()) {
            String groupName = entry.getKey();
            processType = new ProcessType(groupName);
            for (WfDefinition definition : entry.getValue()) {
                processType.addChild(new TreeObject(definition.getId(), definition.getName()));
            }
            root.addChild(processType);
        }
        return root;
    }
}
