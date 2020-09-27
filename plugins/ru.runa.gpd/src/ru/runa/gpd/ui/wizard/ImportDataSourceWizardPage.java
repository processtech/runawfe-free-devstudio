package ru.runa.gpd.ui.wizard;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.sync.WfeServerConnectorSynchronizationCallback;
import ru.runa.gpd.sync.WfeServerDataSourceImporter;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.datasource.DataSourceStuff;

public class ImportDataSourceWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedDataSourcesText;
    private Button selectDataSourcesButton;
    private Button importFromServerButton;
    private WfeServerConnectorComposite serverConnectorComposite;
    private TreeViewer serverDataSourceViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    public ImportDataSourceWizardPage(IStructuredSelection selection) {
        super(ImportDataSourceWizardPage.class, selection);
        setTitle(Localization.getString("ImportDataSourceWizardPage.page.title"));
        setDescription(Localization.getString("ImportDataSourceWizardPage.page.description"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        Group importGroup = new Group(pageControl, SWT.NONE);
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
        selectedDataSourcesText = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedDataSourcesText.setLayoutData(gridData);
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
                    selectedDataSourcesText.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("button.importFromServer"));
        serverConnectorComposite = new WfeServerConnectorComposite(importGroup, WfeServerDataSourceImporter.getInstance(),
                new WfeServerConnectorSynchronizationCallback() {

                    @Override
                    public void onCompleted() {
                        updateServerDataSourceViewer(WfeServerDataSourceImporter.getInstance().getData());
                    }

                    @Override
                    public void onFailed() {
                        updateServerDataSourceViewer(null);
                    }

                });
        createServerDataSourcesGroup(importGroup);
        setControl(pageControl);
        onImportModeChanged();
    }

    private void onImportModeChanged() {
        boolean fromFile = importFromFileButton.getSelection();
        selectedDataSourcesText.setEnabled(fromFile);
        selectDataSourcesButton.setEnabled(fromFile);
        serverConnectorComposite.setEnabled(!fromFile);
        serverDataSourceViewer.getControl().setEnabled(!fromFile);
        if (fromFile) {
            updateServerDataSourceViewer(null);
        } else {
            updateServerDataSourceViewer(WfeServerDataSourceImporter.getInstance().getData());
        }
    }

    private void updateServerDataSourceViewer(Object data) {
        serverDataSourceViewer.setInput(data);
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

    public boolean performFinish() {
        List<DataSourceImportInfo> importInfos = Lists.newArrayList();
        try {
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("ImportDataSourceWizardPage.error.selectValidDs"));
                }
                for (int i = 0; i < selectedFileNames.length; i++) {
                    String dataSourceName = selectedFileNames[i].substring(0, selectedFileNames[i].length() - DataSourceStuff.DATA_SOURCE_ARCHIVE_SUFFIX.length());
                    String fileName = selectedDirFileName + File.separator + selectedFileNames[i];
                    importInfos.add(new DataSourceImportInfo(dataSourceName, new FileInputStream(fileName)));
                }
            } else {
                for (TreeItem treeItem : serverDataSourceViewer.getTree().getSelection()) {
                    importInfos.add(new DataSourceImportInfo(treeItem.getText(),
                            new ByteArrayInputStream(WfeServerDataSourceImporter.getInstance().getDataSourceFile(treeItem.getText()))));
                }
            }
            if (importInfos.isEmpty()) {
                throw new Exception(Localization.getString("ImportDataSourceWizardPage.error.selectValidDataSource"));
            }
            for (DataSourceImportInfo importInfo : importInfos) {
                IFile dsFile = DataSourceUtils.getDataSourcesProject().getFile(importInfo.name + ".xml");
                if (dsFile.exists()) {
                    throw new Exception(Localization.getString("ImportDataSourceWizardPage.error.dataSourceWithSameNameExists", importInfo.name));
                }
                IOUtils.extractArchiveToProject(importInfo.inputStream, DataSourceUtils.getDataSourcesProject());
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

        @SuppressWarnings("unchecked")
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
        private final InputStream inputStream;

        public DataSourceImportInfo(String name, InputStream inputStream) {
            this.name = name;
            this.inputStream = inputStream;
        }

    }

}
