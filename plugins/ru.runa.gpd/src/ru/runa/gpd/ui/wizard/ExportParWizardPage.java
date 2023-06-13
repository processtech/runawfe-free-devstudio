package ru.runa.gpd.ui.wizard;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.aspects.UserActivity;
import ru.runa.gpd.editor.ProcessSaveHistory;
import ru.runa.gpd.lang.model.GlobalSectionDefinition;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.sync.WfeServerConnector;
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.sync.WfeServerProcessDefinitionImporter;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.StatusBarUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.files.FileResourcesExportOperation;
import ru.runa.gpd.util.files.ParFileExporter;
import ru.runa.gpd.util.files.ZipFileExporter;

public class ExportParWizardPage extends ExportWizardPage {
    private final Map<String, IFile> definitionNameFileMap;
    private ListViewer definitionListViewer;
    private Button exportToFileButton;
    private Button exportToServerButton;
    private Button updateLatestVersionButton;
    private WfeServerConnectorComposite serverConnectorComposite;

    protected ExportParWizardPage() {
        super(ExportParWizardPage.class);
        setTitle(Localization.getString("ExportParWizardPage.page.title"));
        setDescription(Localization.getString("ExportParWizardPage.page.description"));
        this.definitionNameFileMap = new TreeMap<String, IFile>();
        for (IFile file : ProcessCache.getAllProcessDefinitionsMap().keySet()) {
            ProcessDefinition definition = ProcessCache.getProcessDefinition(file);
            if (definition != null && !(definition instanceof SubprocessDefinition) && !(definition instanceof GlobalSectionDefinition)) {
                definitionNameFileMap.put(getKey(file, definition), file);
            }
        }
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
        createDestinationDirectoryGroup(exportGroup);
        exportToServerButton = new Button(exportGroup, SWT.RADIO);
        exportToServerButton.setText(Localization.getString("button.exportToServer"));
        updateLatestVersionButton = new Button(exportGroup, SWT.CHECK);
        updateLatestVersionButton.setEnabled(false);
        updateLatestVersionButton.setText(Localization.getString("ExportParWizardPage.page.exportToServer.updateMode"));
        exportToFileButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                onExportModeChanged();
            }
        });

        serverConnectorComposite = new WfeServerConnectorComposite(exportGroup, WfeServerProcessDefinitionImporter.getInstance(), null);
        setControl(pageControl);
        IFile adjacentFile = IOUtils.getCurrentFile();
        if (adjacentFile != null && adjacentFile.getParent().exists()) {
            IFile definitionFile = IOUtils.getProcessDefinitionFile((IFolder) adjacentFile.getParent());
            if (definitionFile.exists()) {
                ProcessDefinition currentDefinition = ProcessCache.getProcessDefinition(definitionFile);
                if (currentDefinition != null && !(currentDefinition instanceof SubprocessDefinition)) {
                    definitionListViewer.setSelection(new StructuredSelection(getKey(definitionFile, currentDefinition)));
                }
            }
        }
        onExportModeChanged();
    }

    private void onExportModeChanged() {
        boolean fromFile = exportToFileButton.getSelection();
        destinationValueText.setEnabled(fromFile);
        browseButton.setEnabled(fromFile);
        updateLatestVersionButton.setEnabled(!exportToFileButton.getSelection());
        serverConnectorComposite.setEnabled(!fromFile);
    }

    @Override
    protected void onBrowseButtonSelected() {
        DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setFilterPath(getDestinationValue());
        String selectedDirectoryName = dialog.open();
        if (selectedDirectoryName != null) {
            setErrorMessage(null);
            if (!selectedDirectoryName.endsWith(File.separator)) {
                selectedDirectoryName += File.separator;
            }
            setDestinationValue(selectedDirectoryName);
        }
    }

    private void createViewer(Composite parent) {
        definitionListViewer = new ListViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        definitionListViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        definitionListViewer.setContentProvider(new ArrayContentProvider());
        definitionListViewer.setInput(definitionNameFileMap.keySet());
        definitionListViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(!event.getSelection().isEmpty());
            }
        });
    }

    private String getKey(IFile definitionFile, ProcessDefinition definition) {
        IProject project = definitionFile.getProject();
        if (IOUtils.isProjectHasProcessNature(project)) {
            String path = definitionFile.getParent().getFullPath().toString();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } else {
            return project.getName() + "/" + definition.getName();
        }
    }

    public boolean finish() {
        final boolean exportToFile = exportToFileButton.getSelection();
        saveDirtyEditors();
        List<String> selectedDefinitionNames = ((IStructuredSelection) definitionListViewer.getSelection()).toList();
        if (selectedDefinitionNames.size() == 0) {
            setErrorMessage(Localization.getString("ExportParWizardPage.error.selectProcess"));
            return false;
        }
        if (exportToFile && Strings.isNullOrEmpty(getDestinationValue())) {
            setErrorMessage(Localization.getString("error.selectDestinationPath"));
            return false;
        }
        if (!exportToFile && WfeServerProcessDefinitionImporter.getInstance().getData() == null) {
            setErrorMessage(Localization.getString("error.selectValidConnection"));
            return false;
        }
        boolean result = true;
        for (final String selectedDefinitionName : selectedDefinitionNames) {
            try {
                final IFile definitionFile = definitionNameFileMap.get(selectedDefinitionName);
                result &= new ParFileExporter(definitionFile).export(exportToFile, (definition, resourcesToExport) -> {
                    if (exportToFile) {
                        if (definition.isInvalid() && !Dialogs
                                .confirm(Localization.getString("ExportParWizardPage.confirm.export.invalid.process", definition.getName()))) {
                            return Optional.empty();
                        }
                        final String outputFileName = getDestinationValue() + definition.getName() + ".par";
                        return Optional
                                .of(new FileResourcesExportOperation(resourcesToExport, new ZipFileExporter(new FileOutputStream(outputFileName))));
                    } else {
                        return Optional.of(new ParDeployOperation(resourcesToExport, definition.getName(), new ByteArrayOutputStream(),
                                updateLatestVersionButton.getSelection()));
                    }
                }, w -> Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(w), e -> setErrorMessage(e));
                if (exportToFile && result && ProcessSaveHistory.isActive()) {
                    IFolder processFolder = (IFolder) definitionFile.getParent();
                    Map<String, File> savepoints = ProcessSaveHistory.getSavepoints(processFolder);
                    if (savepoints.size() > 0) {
                        List<File> filesToExport = new ArrayList<>();
                        for (Map.Entry<String, File> savepoint : savepoints.entrySet()) {
                            filesToExport.add(savepoint.getValue());
                        }
                        filesToExport.add(new File(getDestinationValue() + processFolder.getName() + ".par"));
                        String oldestSavepointName = ((NavigableMap<String, File>) savepoints).lastEntry().getValue().getName();
                        String oldestTimestamp = oldestSavepointName.substring(oldestSavepointName.lastIndexOf("_") + 1,
                                oldestSavepointName.lastIndexOf("."));
                        Map<String, File> uaLogs = UserActivity.getLogs(processFolder);
                        for (Map.Entry<String, File> uaLog : uaLogs.entrySet()) {
                            if (oldestTimestamp.compareTo(uaLog.getKey()) <= 0) {
                                filesToExport.add(uaLog.getValue());
                            }
                        }
                        zip(filesToExport, new FileOutputStream(getDestinationValue() + processFolder.getName() + ".har"));
                    }
                }
            } catch (Throwable th) {
                PluginLogger.logErrorWithoutDialog(Localization.getString("ExportParWizardPage.error.export"), th);
                setErrorMessage(stripExceptionMessage(Throwables.getRootCause(th).getMessage()));
                return false;
            }
        }
        return result;
    }

    private String stripExceptionMessage(String message) {
        if (message == null) {
            return "";
        }
        if (message.contains("Connection refused: connect")) {
            return Localization.getString("ExportParWizardPage.error.simulator.start");
        }
        if (message.contains("Unable to acquire version using")) {
            return Localization.getString("ExportParWizardPage.error.simulator.connect");
        }
        return stripSoapTrash(message);
    }

    private String stripSoapTrash(String trash) {
        final String head = "exception:";
        final String tail = "please see the server log to find more detail regarding exact cause of the failure.";
        int index = trash.toLowerCase().lastIndexOf(head);
        String tag = (index > 0 ? trash.substring(index + head.length()) : trash);
        if (tag.toLowerCase().endsWith(tail)) {
            tag = tag.substring(0, tag.length() - tail.length());
        }
        return tag.trim();
    }

    public static class ParDeployOperation extends FileResourcesExportOperation {
        private final ByteArrayOutputStream outputStream;
        private final String definitionName;
        private final boolean updateLatestVersion;

        public ParDeployOperation(List<IFile> resourcesToExport, String definitionName, ByteArrayOutputStream outputStream,
                boolean updateLatestVersion) throws IOException {
            super(resourcesToExport, new ZipFileExporter(outputStream));
            this.outputStream = outputStream;
            this.definitionName = definitionName;
            this.updateLatestVersion = updateLatestVersion;
        }

        @Override
        public void exportResources(final IProgressMonitor progressMonitor) {
            super.exportResources(progressMonitor);
            WfeServerProcessDefinitionImporter.getInstance().uploadPar(definitionName, updateLatestVersion, outputStream.toByteArray(), true);
            String message;
            if (updateLatestVersion) {
                message = Localization.getString("ExportParWizardPage.par.update.completed");
            } else {
                message = Localization.getString("ExportParWizardPage.par.deploy.completed");
            }
            StatusBarUtils.updateStatusBar(message + " " + WfeServerConnector.getInstance().getSettings().getUrl());
        }
    }

    private void zip(List<File> files, OutputStream os) throws IOException, CoreException {
        ZipOutputStream zos = new ZipOutputStream(os);
        for (File file : files) {
            ZipEntry newEntry = new ZipEntry(file.getName());
            byte[] readBuffer = new byte[1024];
            zos.putNextEntry(newEntry);
            InputStream cos = new FileInputStream(file);
            try {
                int n;
                while ((n = cos.read(readBuffer)) > 0) {
                    zos.write(readBuffer, 0, n);
                }
            } finally {
                cos.close();
            }
            zos.closeEntry();
        }
        zos.close();
    }

}
