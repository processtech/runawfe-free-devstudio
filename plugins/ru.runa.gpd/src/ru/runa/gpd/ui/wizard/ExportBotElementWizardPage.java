package ru.runa.gpd.ui.wizard;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.io.File;
import java.util.Map;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.sync.WfeServerBotImporter;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public abstract class ExportBotElementWizardPage extends ExportWizardPage {
    protected Map<String, IResource> exportObjectNameFileMap;
    protected final IResource exportResource;
    private ListViewer exportResourceListViewer;
    private Button exportToFileButton;
    private Button exportToServerButton;
    private WfeServerConnectorComposite serverConnectorComposite;

    public ExportBotElementWizardPage(Class<? extends ExportWizardPage> clazz, IStructuredSelection selection) {
        super(clazz);
        this.exportResource = getInitialElement(selection);
    }

    private IResource getInitialElement(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;
                IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                return resource;
            }
        }
        return null;
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        Group processListGroup = new Group(sashForm, SWT.NONE);
        processListGroup.setLayout(new GridLayout(1, false));
        processListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        processListGroup.setText(Localization.getString("label.process"));
        createViewer(processListGroup);
        Group exportGroup = new Group(sashForm, SWT.NONE);
        exportGroup.setLayout(new GridLayout(1, false));
        exportGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        exportToFileButton = new Button(exportGroup, SWT.RADIO);
        exportToFileButton.setText(Localization.getString("button.exportToFile"));
        exportToFileButton.setSelection(true);
        exportToFileButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                onExportModeChanged();
            }
        });
        createDestinationDirectoryGroup(exportGroup);
        exportToServerButton = new Button(exportGroup, SWT.RADIO);
        exportToServerButton.setText(Localization.getString("button.exportToServer"));
        serverConnectorComposite = new WfeServerConnectorComposite(exportGroup, WfeServerBotImporter.getInstance(), null);
        setControl(pageControl);
        if (exportResource != null) {
            exportResourceListViewer.setSelection(new StructuredSelection(getSelectionResourceKey(exportResource)));
        }
        onExportModeChanged();
    }

    private void onExportModeChanged() {
        boolean fromFile = exportToFileButton.getSelection();
        destinationValueText.setEnabled(fromFile);
        browseButton.setEnabled(fromFile);
        serverConnectorComposite.setEnabled(!fromFile);
    }

    @Override
    protected void onBrowseButtonSelected() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { getOutputSuffix(), "*.*" });
        String selectionName = getBotElementSelection();
        if (selectionName != null) {
            dialog.setFileName(getFileName(selectionName));
        }
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString.lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
            dialog.setFilterPath(currentSourceString.substring(0, lastSeparatorIndex));
        }
        String selectedFileName = dialog.open();
        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
        }
    }

    private void createViewer(Composite parent) {
        exportResourceListViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        exportResourceListViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        exportResourceListViewer.setContentProvider(new ArrayContentProvider());
        exportResourceListViewer.setInput(exportObjectNameFileMap.keySet());
        exportResourceListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(!event.getSelection().isEmpty());
            }
        });
    }

    protected String getKey(IProject project, IResource resource) {
        return project.getName() + "/" + resource.getName();
    }

    private String getBotElementSelection() {
        return (String) ((IStructuredSelection) exportResourceListViewer.getSelection()).getFirstElement();
    }

    protected String getFileName(String selectionName) {
        return selectionName.substring(selectionName.lastIndexOf("/") + 1) + getOutputSuffix();
    }

    public boolean finish() {
        boolean exportToFile = exportToFileButton.getSelection();
        saveDirtyEditors();
        String selected = getBotElementSelection();
        if (selected == null) {
            setErrorMessage("ExportBotElementWizardPage.error.empty.source.selection");
            return false;
        }
        if (exportToFile && Strings.isNullOrEmpty(getDestinationValue())) {
            setErrorMessage(Localization.getString("error.selectDestinationPath"));
            return false;
        }
        if (!exportToFile && !WfeServerConnector.getInstance().isConfigured()) {
            setErrorMessage(Localization.getString("error.selectValidConnection"));
            return false;
        }
        IResource exportResource = exportObjectNameFileMap.get(selected);
        try {
            exportResource.refreshLocal(IResource.DEPTH_ONE, null);
            if (exportToFile) {
                exportToZipFile(exportResource);
            } else {
                deployToServer(exportResource);
            }
            return true;
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog("ExportBotElementWizardPage.error.export", th);
            setErrorMessage(Throwables.getRootCause(th).getMessage());
            return false;
        }
    }

    protected abstract void exportToZipFile(IResource exportResource) throws Exception;

    protected abstract void deployToServer(IResource exportResource) throws Exception;

    protected abstract String getOutputSuffix();

    protected abstract String getSelectionResourceKey(IResource resource);

}
