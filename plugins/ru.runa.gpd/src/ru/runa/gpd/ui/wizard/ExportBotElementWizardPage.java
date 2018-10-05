package ru.runa.gpd.ui.wizard;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.wfe.WFEServerBotElementImporter;

import com.google.common.base.Throwables;

@SuppressWarnings("restriction")
public abstract class ExportBotElementWizardPage extends WizardArchiveFileResourceExportPage1 {
    protected Map<String, IResource> exportObjectNameFileMap;
    protected final IResource exportResource;
    private ListViewer exportResourceListViewer;
    private Button exportToFileButton;
    private Button exportToServerButton;

    public ExportBotElementWizardPage(IStructuredSelection selection) {
        super(selection);
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
        exportToFileButton.setText(Localization.getString("ExportParWizardPage.page.exportToFileButton"));
        exportToFileButton.setSelection(true);
        createDestinationGroup(exportGroup);
        exportToServerButton = new Button(exportGroup, SWT.RADIO);
        exportToServerButton.setText(Localization.getString("ExportParWizardPage.page.exportToServerButton"));
        SyncUIHelper.createHeader(exportGroup, WFEServerBotElementImporter.getInstance(), WFEConnectionPreferencePage.class, null);
        restoreWidgetValues();
        giveFocusToDestination();
        setControl(pageControl);
        setPageComplete(false);
        if (exportResource != null) {
            exportResourceListViewer.setSelection(new StructuredSelection(getSelectionResourceKey(exportResource)));
        }
    }

    private void createViewer(Composite parent) {
        // process selection
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

    @Override
    protected String getDestinationLabel() {
        return Localization.getString("ExportParWizardPage.label.destination_file");
    }

    @Override
    protected void handleDestinationBrowseButtonPressed() {
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

    protected String getFileName(String selectionName) {
        return selectionName.substring(selectionName.lastIndexOf("/") + 1) + getOutputSuffix();
    }

    @Override
    protected void updatePageCompletion() {
        setPageComplete(true);
    }

    @Override
    public boolean finish() {
        boolean exportToFile = exportToFileButton.getSelection();
        // Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();
        String selected = getBotElementSelection();
        if (selected == null) {
            setErrorMessage("select");
            return false;
        }
        if (exportToFile && !ensureTargetIsValid()) {
            setErrorMessage(Localization.getString("ExportParWizardPage.error.selectDestinationPath"));
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
            PluginLogger.logErrorWithoutDialog("botelement.error.export", th);
            setErrorMessage(Throwables.getRootCause(th).getMessage());
            return false;
        }
    }

    protected abstract void exportToZipFile(IResource exportResource) throws Exception;

    protected abstract void deployToServer(IResource exportResource) throws Exception;

    @Override
    protected abstract String getOutputSuffix();

    protected abstract String getSelectionResourceKey(IResource resource);

    private final static String STORE_DESTINATION_NAMES_ID = "WizardParExportPage1.STORE_DESTINATION_NAMES_ID";

    @Override
    protected void internalSaveWidgetValues() {
        // update directory names history
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null) {
                directoryNames = new String[0];
            }
            directoryNames = addToHistory(directoryNames, getDestinationValue());
            settings.put(STORE_DESTINATION_NAMES_ID, directoryNames);
        }
    }

    @Override
    protected void restoreWidgetValues() {
        IDialogSettings settings = getDialogSettings();
        if (settings != null) {
            String[] directoryNames = settings.getArray(STORE_DESTINATION_NAMES_ID);
            if (directoryNames == null || directoryNames.length == 0) {
                return; // ie.- no settings stored
            }
            // destination
            setDestinationValue(directoryNames[0]);
            for (int i = 0; i < directoryNames.length; i++) {
                addDestinationItem(directoryNames[i]);
            }
        }
    }
}
