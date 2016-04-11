package ru.runa.gpd.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
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

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.wfe.ConnectorCallback;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.gpd.wfe.WFEServerBotElementImporter;
import ru.runa.gpd.wfe.WFEServerBotStationElementImporter;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;

import com.google.common.base.Throwables;

public abstract class ImportBotElementWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedParsLabel;
    private Button selectParsButton;
    protected Button importFromServerButton;
    protected TreeViewer serverDataViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;
    protected final IResource importResource;
    protected Map<String, IResource> importObjectNameFileMap = new TreeMap<String, IResource>();

    public ImportBotElementWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
        this.importResource = getInitialElement(selection);
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
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        createBotStationsGroup(sashForm);
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
                // dialog.setFileName(startingDirectory.getPath());
                dialog.setFilterExtensions(new String[] { getInputSuffix() });
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
        SyncUIHelper.createHeader(importGroup, getDataImporter(), WFEConnectionPreferencePage.class, new ConnectorCallback() {

            @Override
            public void onSynchronizationCompleted() {
                populateInputView();
            }

            @Override
            public void onSynchronizationFailed(Exception e) {
                Dialogs.error(Localization.getString("error.Synchronize"), e);
            }
            
        });
        createServerDataGroup(importGroup);
        setControl(pageControl);
    }

    private void setImportMode() {
        boolean fromFile = importFromFileButton.getSelection();
        // editor.setEnabled(fromFile, fileSelectionArea);
        selectParsButton.setEnabled(fromFile);
        if (fromFile) {
            serverDataViewer.setInput(new Object());
            //serverDefinitionViewer.refresh(true);
        } else {
            if (getDataImporter().isConfigured()) {
                if (!getDataImporter().hasCachedData()) {
                    long start = System.currentTimeMillis();
                    getDataImporter().synchronize();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo("def sync [sec]: " + ((end - start) / 1000));
                }
                populateInputView();
                serverDataViewer.refresh(true);
            }
        }
    }

    protected abstract void populateInputView();

    private void createServerDataGroup(Composite parent) {
        serverDataViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        serverDataViewer.getControl().setLayoutData(gridData);
        serverDataViewer.setContentProvider(getContentProvider());
        serverDataViewer.setLabelProvider(new BotStationLabelProvider());
        serverDataViewer.setInput(new Object());
    }

    protected abstract Class<?> getBotElementClass();

    public boolean performFinish() {
        try {
            //IProject project = getSelectedProject();
            String[] processNames;
            InputStream[] parInputStreams;
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
                TreePath[] selections = ((ITreeSelection) serverDataViewer.getSelection()).getPaths();
                List<TreePath> defSelections = new ArrayList<TreePath>();
                for (TreePath object : selections) {
                    defSelections.add(object);
                }
                if (defSelections.isEmpty()) {
                    throw new Exception(Localization.getString("ImportParWizardPage.error.selectValidDefinition"));
                }
                processNames = new String[defSelections.size()];
                parInputStreams = new InputStream[defSelections.size()];
                for (int i = 0; i < processNames.length; i++) {
                    TreePath treePath = defSelections.get(i);
                    Object obj = treePath.getLastSegment();
                    if (obj instanceof BotStation) {
                        BotStation botStation = (BotStation) obj;
                        processNames[i] = botStation.getName();
                        byte[] par = ((WFEServerBotStationElementImporter) getDataImporter()).getBotStationFile(botStation);
                        parInputStreams[i] = new ByteArrayInputStream(par);
                    } else if (obj instanceof Bot) {
                        Bot bot = (Bot) obj;
                        processNames[i] = bot.getUsername();
                        byte[] par = ((WFEServerBotElementImporter) getDataImporter()).getBotFile(bot);
                        parInputStreams[i] = new ByteArrayInputStream(par);
                    } else if (obj instanceof BotTask) {
                        BotTask botTask = (BotTask) obj;
                        processNames[i] = botTask.getName();
                        byte[] par = ((WFEServerBotElementImporter) getDataImporter()).getBotTaskFile((Bot) treePath.getFirstSegment(), botTask.getName());
                        parInputStreams[i] = new ByteArrayInputStream(par);
                    }
                }
            }
            for (int i = 0; i < processNames.length; i++) {
                String processName = processNames[i];
                InputStream parInputStream = parInputStreams[i];
                runImport(parInputStream, processName);
            }
        } catch (Throwable th) {
            PluginLogger.logErrorWithoutDialog("import bot element", th);
            setErrorMessage(Throwables.getRootCause(th).getMessage());
            return false;
        }
        return true;
    }

    public abstract void runImport(InputStream parInputStream, String botName) throws InvocationTargetException, InterruptedException;

    protected abstract String getInputSuffix();

    protected abstract String getSelectionResourceKey(IResource resource);

    protected String getKey(IProject project, IResource resource) {
        return project.getName() + "/" + resource.getName();
    }

    protected String getBotElementSelection() {
        return (String) ((IStructuredSelection) projectViewer.getSelection()).getFirstElement();
    }

    public static class BotStationLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (element instanceof BotStation) {
                return ((BotStation) element).getName();
            } else if (element instanceof Bot) {
                return ((Bot) element).getUsername();
            } else if (element instanceof BotTask) {
                return ((BotTask) element).getName();
            }
            return super.getText(element);
        }
    }

    protected void createBotStationsGroup(Composite parent) {
        Group projectListGroup = new Group(parent, SWT.NONE);
        projectListGroup.setLayout(new GridLayout(1, false));
        projectListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectListGroup.setText(Localization.getString("label.project"));
        createBotStationsList(projectListGroup);
    }

    private void createBotStationsList(Composite parent) {
        projectViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        projectViewer.getControl().setLayoutData(gridData);
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setInput(importObjectNameFileMap.keySet());
        if (importResource != null) {
            projectViewer.setSelection(new StructuredSelection(getSelectionResourceKey(importResource)));
        }
    }

    protected abstract DataImporter getDataImporter();

    protected abstract Object[] getBotElements();

    protected abstract ITreeContentProvider getContentProvider();
}
