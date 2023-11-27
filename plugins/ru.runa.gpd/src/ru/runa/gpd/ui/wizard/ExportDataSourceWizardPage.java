package ru.runa.gpd.ui.wizard;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.sync.WfeServerDataSourceImporter;
import ru.runa.gpd.sync.WfeServerProcessDefinitionImporter;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.DataSourceUtils;
import ru.runa.wfe.datasource.DataSourceStuff;

public class ExportDataSourceWizardPage extends ExportWizardPage {
    private final Map<String, IFile> dataSourceNameFileMap;
    private ListViewer dataSourceListViewer;
    protected final IResource exportResource;
    private Button exportToFileButton;
    private Button exportToServerButton;
    private WfeServerConnectorComposite serverConnectorComposite;

    protected ExportDataSourceWizardPage(IStructuredSelection selection) {
        super(ExportDataSourceWizardPage.class);
        setTitle(Localization.getString("ExportDataSourceWizard.wizard.title"));
        setDescription(Localization.getString("ExportDataSourceWizardPage.page.description"));
        dataSourceNameFileMap = new TreeMap<String, IFile>();
        for (IFile file : DataSourceUtils.getAllDataSources()) {
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
        exportToFileButton.setText(Localization.getString("button.exportToFile"));
        exportToFileButton.setSelection(true);
        exportToFileButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                onExportModeChanged();
            }
        });
        createDestinationDirectoryGroup(exportGroup, false);
        exportToServerButton = new Button(exportGroup, SWT.RADIO);
        exportToServerButton.setText(Localization.getString("button.exportToServer"));
        serverConnectorComposite = new WfeServerConnectorComposite(exportGroup, WfeServerProcessDefinitionImporter.getInstance(), null);
        setControl(pageControl);
        if (exportResource != null) {
            String name = exportResource.getName();
            dataSourceListViewer.setSelection(new StructuredSelection(name.substring(0, name.lastIndexOf('.'))));
        }
        onExportModeChanged();
    }

    private void onExportModeChanged() {
        boolean fromFile = exportToFileButton.getSelection();
        destinationValueText.setEnabled(fromFile);
        browseButton.setEnabled(fromFile);
        serverConnectorComposite.setEnabled(!fromFile);
    }

    private void createViewer(Composite parent) {
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

    private String getSelection() {
        return (String) ((IStructuredSelection) dataSourceListViewer.getSelection()).getFirstElement();
    }

    private String getFileName(String selectionName) {
        return selectionName.substring(selectionName.lastIndexOf("/") + 1) + DataSourceStuff.DATA_SOURCE_ARCHIVE_SUFFIX;
    }

    @Override
    protected void onBrowseButtonSelected() {
        FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterExtensions(new String[] { DataSourceStuff.DATA_SOURCE_ARCHIVE_SUFFIX, "*.*" });
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

    public boolean finish() {
        boolean exportToFile = exportToFileButton.getSelection();
        String selected = getSelection();
        if (selected == null) {
            setErrorMessage("select");
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
        IResource exportResource = dataSourceNameFileMap.get(selected);
        try {
            exportResource.refreshLocal(IResource.DEPTH_ONE, null);
            if (exportToFile) {
                exportToZipFile(exportResource);
            } else {
                String name = exportResource.getName();
                name = name.substring(0, name.lastIndexOf('.'));
                List<String> serverDataSourceNames = WfeServerDataSourceImporter.getInstance().getData();
                if (!serverDataSourceNames.contains(name)
                        || Dialogs.confirm(Localization.getString("ExportDataSourceWizardPage.error.dataSourceWithSameNameExists", name))) {
                    deployToServer(exportResource);
                } else {
                    return false;
                }
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

    private static class DsExportOperation implements IRunnableWithProgress {
        
        protected final OutputStream outputStream;
        protected final List<IFile> resourcesToExport;

        public DsExportOperation(List<IFile> resourcesToExport, OutputStream outputStream) {
            this.outputStream = outputStream;
            this.resourcesToExport = resourcesToExport;
        }

        protected void exportResource(DsFileExporter exporter, IFile fileResource, IProgressMonitor progressMonitor)
                throws IOException, CoreException {
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
                        WfeServerConnector.getInstance().deployDataSourceArchive(baos.toByteArray());
                    }
                });
            } catch (Exception e) {
                throw new InvocationTargetException(e);
            }
        }
    }

    private static class DsFileExporter {
        
        private final ZipOutputStream outputStream;

        public DsFileExporter(OutputStream outputStream) throws IOException {
            this.outputStream = new ZipOutputStream(outputStream, Charsets.UTF_8);
        }

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

        public void write(IFile resource, String destinationPath) throws IOException, CoreException {
            ZipEntry newEntry = new ZipEntry(destinationPath);
            write(newEntry, resource);
        }

    }

}
