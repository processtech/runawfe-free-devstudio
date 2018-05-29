package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.internal.wizards.datatransfer.IFileExporter;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.wfe.WFEServerDataSourceImporter;
import ru.runa.gpd.wfe.WFEServerProcessDefinitionImporter;
import ru.runa.wfe.datasource.DataSourceStuff;

@SuppressWarnings("restriction")
public class ExportDataSourceWizardPage extends WizardArchiveFileResourceExportPage1 {

    private final Map<String, IFile> dataSourceNameFileMap;
    private ListViewer dataSourceListViewer;
    protected final IResource exportResource;
    private Button exportToFileButton;
    private Button exportToServerButton;

    protected ExportDataSourceWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ExportDataSourceWizard.wizard.title"));
        setDescription(Localization.getString("ExportDataSourceWizardPage.page.description"));
        dataSourceNameFileMap = new TreeMap<String, IFile>();
        for (IFile file : IOUtils.getAllDataSources()) {
            String name = file.getName();
            dataSourceNameFileMap.put(name.substring(0, name.lastIndexOf('.')), file);
        }
        exportResource = getInitialElement(selection);
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
        processListGroup.setText(Localization.getString("label.view.dataSourceDesignerExplorer"));
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
        SyncUIHelper.createHeader(exportGroup, WFEServerProcessDefinitionImporter.getInstance(), WFEConnectionPreferencePage.class, null);
        restoreWidgetValues();
        giveFocusToDestination();
        setControl(pageControl);
        setPageComplete(false);
        if (exportResource != null) {
            String name = exportResource.getName();
            dataSourceListViewer.setSelection(new StructuredSelection(name.substring(0, name.lastIndexOf('.'))));
        }
    }

    private void createViewer(Composite parent) {
        // process selection
        dataSourceListViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        dataSourceListViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        dataSourceListViewer.setContentProvider(new ArrayContentProvider());
        dataSourceListViewer.setInput(dataSourceNameFileMap.keySet());
        dataSourceListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(!event.getSelection().isEmpty());
            }
        });
    }

    @Override
    protected String getDestinationLabel() {
        return Localization.getString("ExportParWizardPage.label.destination_file");
    }

    private String getSelection() {
        return (String) ((IStructuredSelection) dataSourceListViewer.getSelection()).getFirstElement();
    }

    private String getFileName(String selectionName) {
        return selectionName.substring(selectionName.lastIndexOf("/") + 1) + getOutputSuffix();
    }

    @Override
    protected void handleDestinationBrowseButtonPressed() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { getOutputSuffix(), "*.*" });
        String selectionName = getSelection();
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

    @Override
    protected void updatePageCompletion() {
        setPageComplete(true);
    }

    @Override
    protected String getOutputSuffix() {
        return DataSourceStuff.DATA_SOURCE_ARCHIVE_SUFFIX;
    }

    @Override
    public boolean finish() {
        boolean exportToFile = exportToFileButton.getSelection();
        // Save dirty editors if possible but do not stop if not all are saved
        //saveDirtyEditors();
        // about to invoke the operation so save our state
        saveWidgetValues();
        String selected = getSelection();
        if (selected == null) {
            setErrorMessage("select");
            return false;
        }
        if (exportToFile && getDestinationValue().isEmpty()) {
            setErrorMessage(Localization.getString("ExportParWizardPage.error.selectDestinationPath"));
            return false;
        }
        IResource exportResource = dataSourceNameFileMap.get(selected);
        try {
            exportResource.refreshLocal(IResource.DEPTH_ONE, null);
            if (exportToFile) {
                exportToZipFile(exportResource);
            } else {
                deployToServer(exportResource);
            }
            return true;
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog("datasource.error.export", th);
            setErrorMessage(Throwables.getRootCause(th).getMessage());
            return false;
        }
    }

    protected void exportToZipFile(IResource exportResource) throws Exception {
        new DsExportOperation(Lists.newArrayList((IFile) exportResource), new FileOutputStream(getDestinationValue())).run(null);
    }

    protected void deployToServer(IResource exportResource) throws Exception {
        new DataSourceDeployOperation(Lists.newArrayList((IFile) exportResource)).run(null);
    }

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

    private static class DsExportOperation implements IRunnableWithProgress {
        
        protected final OutputStream outputStream;
        protected final List<IFile> resourcesToExport;

        public DsExportOperation(List<IFile> resourcesToExport, OutputStream outputStream) {
            this.outputStream = outputStream;
            this.resourcesToExport = resourcesToExport;
        }

        protected void exportResource(IFileExporter exporter, IFile fileResource, IProgressMonitor progressMonitor) throws IOException, CoreException {
            if (!fileResource.isSynchronized(IResource.DEPTH_ONE)) {
                fileResource.refreshLocal(IResource.DEPTH_ONE, null);
            }
            if (!fileResource.isAccessible()) {
                return;
            }
            String destinationName = fileResource.getName();
            exporter.write(fileResource, destinationName);
        }

        protected void exportResources(IProgressMonitor progressMonitor) throws InvocationTargetException {
            try {
                DsFileExporter exporter = new DsFileExporter(outputStream);
                for (IFile resource : resourcesToExport) {
                    exportResource(exporter, resource, progressMonitor);
                }
                exporter.finished();
                outputStream.flush();
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }

        @Override
        public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
            exportResources(progressMonitor);
        }
    
    }

    private class DataSourceDeployOperation extends DsExportOperation {
        
        public DataSourceDeployOperation(List<IFile> resourcesToExport) {
            super(resourcesToExport, new ByteArrayOutputStream());
        }

        @Override
        public void run(final IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
            exportResources(progressMonitor);
            final ByteArrayOutputStream baos = (ByteArrayOutputStream) outputStream;
            try {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        WFEServerDataSourceImporter.getInstance().deployDataSource(baos.toByteArray());
                    }
                });
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    private static class DsFileExporter implements IFileExporter {
        
        private final ZipOutputStream outputStream;

        public DsFileExporter(OutputStream outputStream) throws IOException {
            this.outputStream = new ZipOutputStream(outputStream, Charsets.UTF_8);
        }

        @Override
        public void finished() throws IOException {
            outputStream.close();
        }

        private void write(ZipEntry entry, IFile contents) throws IOException, CoreException {
            byte[] readBuffer = new byte[1024];
            outputStream.putNextEntry(entry);
            InputStream contentStream = contents.getContents();
            try {
                int n;
                while ((n = contentStream.read(readBuffer)) > 0) {
                    outputStream.write(readBuffer, 0, n);
                }
            } finally {
                if (contentStream != null) {
                    contentStream.close();
                }
            }
            outputStream.closeEntry();
        }

        @Override
        public void write(IFile resource, String destinationPath) throws IOException, CoreException {
            ZipEntry newEntry = new ZipEntry(destinationPath);
            write(newEntry, resource);
        }

        @Override
        public void write(IContainer container, String destinationPath) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

}
