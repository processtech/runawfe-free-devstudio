package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.wfe.ConnectorCallback;
import ru.runa.gpd.wfe.WFEServerDataSourceImporter;
import ru.runa.wfe.datasource.DataSourceStuff;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class ImportDataSourceWizardPage extends WizardPage {
    
    private ListViewer projectViewer;
    private final IContainer initialSelection;
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedDataSourcesLabel;
    private Button selectDataSourcesButton;
    private Button importFromServerButton;
    private TreeViewer serverDataSourceViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    public ImportDataSourceWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName);
        initialSelection = (IContainer) IOUtils.getProcessSelectionResource(selection);
        setTitle(Localization.getString("ImportDataSourceWizardPage.page.title"));
        setDescription(Localization.getString("ImportDataSourceWizardPage.page.description"));
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
        selectedDataSourcesLabel = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedDataSourcesLabel.setLayoutData(gridData);
        selectDataSourcesButton = new Button(fileSelectionArea, SWT.PUSH);
        selectDataSourcesButton.setText(Localization.getString("button.choose"));
        selectDataSourcesButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectDataSourcesButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { "*.ds" });
                if (dialog.open() != null) {
                    selectedDirFileName = dialog.getFilterPath();
                    selectedFileNames = dialog.getFileNames();
                    String text = "";
                    for (String fileName : selectedFileNames) {
                        text += fileName + "\n";
                    }
                    selectedDataSourcesLabel.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("ImportParWizardPage.page.importFromServerButton"));
        SyncUIHelper.createHeader(importGroup, WFEServerDataSourceImporter.getInstance(), WFEConnectionPreferencePage.class,
                new ConnectorCallback() {

                    @Override
                    public void onSynchronizationFailed(Exception e) {
                        Dialogs.error(Localization.getString("error.Synchronize"), e);
                    }

                    @Override
                    public void onSynchronizationCompleted() {
                        setupServerDataSourceViewer();
                    }
                });
        createServerDataSourcesGroup(importGroup);
        setControl(pageControl);
    }

    private void createProjectsGroup(Composite parent) {
        Group projectListGroup = new Group(parent, SWT.NONE);
        projectListGroup.setLayout(new GridLayout());
        projectListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectListGroup.setText(Localization.getString("label.project"));
        createProjectsList(projectListGroup);
    }

    private void createProjectsList(Composite parent) {
        projectViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        projectViewer.getControl().setLayoutData(gridData);
        projectViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((IContainer) element).getName();
            }
        });
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setInput(Lists.newArrayList(IOUtils.getDataSourcesProject()));
        if (initialSelection != null) {
            projectViewer.setSelection(new StructuredSelection(initialSelection));
        }
    }

    private void setImportMode() {
        boolean fromFile = importFromFileButton.getSelection();
        selectDataSourcesButton.setEnabled(fromFile);
        if (fromFile) {
            serverDataSourceViewer.setInput(new Object());
        } else {
            if (WFEServerDataSourceImporter.getInstance().isConfigured()) {
                if (!WFEServerDataSourceImporter.getInstance().hasCachedData()) {
                    long start = System.currentTimeMillis();
                    WFEServerDataSourceImporter.getInstance().synchronize();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo("def sync [sec]: " + ((end - start) / 1000));
                }
                setupServerDataSourceViewer();
            }
        }
    }

    private void setupServerDataSourceViewer() {
        List<String> dataSourceNames = WFEServerDataSourceImporter.getInstance().getDataSourceNames();
        serverDataSourceViewer.setInput(dataSourceNames);
        serverDataSourceViewer.refresh(true);
    }

    private void createServerDataSourcesGroup(Composite parent) {
        serverDataSourceViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        serverDataSourceViewer.getControl().setLayoutData(gridData);
        serverDataSourceViewer.setContentProvider(new ViewContentProvider());
        serverDataSourceViewer.setLabelProvider(new LabelProvider());
        serverDataSourceViewer.setInput(new Object());
    }

    private IContainer getSelectedContainer() throws Exception {
        IStructuredSelection selectedProject = (IStructuredSelection) projectViewer.getSelection();
        IContainer container = (IContainer) selectedProject.getFirstElement();
        if (container == null) {
            throw new Exception(Localization.getString("ImportParWizardPage.error.selectTargetProject"));
        }
        return container;
    }

    public boolean performFinish() {
        List<DataSourceImportInfo> importInfos = Lists.newArrayList();
        try {
            IContainer container = getSelectedContainer();
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("ImportDataSourceWizardPage.error.selectValidDs"));
                }
                for (int i = 0; i < selectedFileNames.length; i++) {
                    String dataSourceName = selectedFileNames[i].substring(0, selectedFileNames[i].length() - DataSourceStuff.DATA_SOURCE_ARCHIVE_SUFFIX.length());
                    String fileName = selectedDirFileName + File.separator + selectedFileNames[i];
                    importInfos.add(new DataSourceImportInfo(dataSourceName, "", new FileInputStream(fileName)));
                }
            } else {
                for (TreeItem treeItem : serverDataSourceViewer.getTree().getSelection()) {
                    // DefinitionTreeNode treeNode = (DefinitionTreeNode) treeItem.getData();
                    // importInfos.addAll(treeNode.toRecursiveImportInfo(""));
                    importInfos.add(new DataSourceImportInfo(treeItem.getText(), "", 
                            new ByteArrayInputStream(WFEServerDataSourceImporter.getInstance().getDataSourceFile(treeItem.getText()))));
                }
            }
            if (importInfos.isEmpty()) {
                throw new Exception(Localization.getString("ImportDataSourceWizardPage.error.selectValidDataSource"));
            }
            for (DataSourceImportInfo importInfo : importInfos) {
                IFile dsFile = IOUtils.getDataSourcesProject().getFile(importInfo.name + ".xml");
                if (dsFile.exists()) {
                    throw new Exception(Localization.getString("ImportDataSourceWizardPage.error.dataSourceWithSameNameExists", importInfo.name));
                }
                IOUtils.extractArchiveToProject(importInfo.inputStream, IOUtils.getDataSourcesProject());
            }
        } catch (Exception exception) {
            PluginLogger.logErrorWithoutDialog("import ds", exception);
            setErrorMessage(Throwables.getRootCause(exception).getMessage());
            return false;
        } finally {
            for (DataSourceImportInfo importInfo : importInfos) {
                try {
                    importInfo.inputStream.close();
                } catch (Exception e) {
                }
            }
        }
        return true;
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
            return null;
        }

        @Override
        public Object[] getChildren(Object parent) {
            return parent instanceof List<?> ? ((List<String>) parent).toArray(new String[] {}) : new Object[0];
        }

        @Override
        public boolean hasChildren(Object parent) {
            return false;
        }
    }

    class DataSourceImportInfo {
        private final String name;
        private final String path;
        private final InputStream inputStream;

        public DataSourceImportInfo(String name, String path, InputStream inputStream) {
            this.name = name;
            this.path = path;
            this.inputStream = inputStream;
        }

        private String getFolderPath() {
            if (path.trim().isEmpty()) {
                return name;
            }
            return path + File.separator + name;
        }
    }

}
