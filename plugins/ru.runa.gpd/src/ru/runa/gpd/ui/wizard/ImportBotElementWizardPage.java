package ru.runa.gpd.ui.wizard;

import com.google.common.base.Throwables;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
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
import ru.runa.gpd.sync.WfeServerBotImporter;
import ru.runa.gpd.sync.WfeServerBotStationImporter;
import ru.runa.gpd.sync.WfeServerConnectorComposite;
import ru.runa.gpd.sync.WfeServerConnectorDataImporter;
import ru.runa.gpd.sync.WfeServerConnectorSynchronizationCallback;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;

public abstract class ImportBotElementWizardPage extends ImportWizardPage {
    private Button importFromFileButton;
    private Composite fileSelectionArea;
    private Text selectedElementsText;
    private Button selectParsButton;
    private Button importFromServerButton;
    private WfeServerConnectorComposite serverConnectorComposite;
    private TreeViewer serverDataViewer;
    private String selectedDirFileName;
    private String[] selectedFileNames;

    public ImportBotElementWizardPage(Class<? extends ImportWizardPage> clazz, IStructuredSelection selection) {
        super(clazz, selection);
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        List<? extends IContainer> projectData = getProjectDataViewerInput();
        if (projectData != null) {
            createProjectsGroup(sashForm, projectData, getProjectDataViewerLabelProvider());
        }
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
        selectedElementsText = new Text(fileSelectionArea, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedElementsText.setLayoutData(gridData);
        selectParsButton = new Button(fileSelectionArea, SWT.PUSH);
        selectParsButton.setText(Localization.getString("button.choose"));
        selectParsButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParsButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);
                dialog.setFilterExtensions(new String[] { getFilterExtension() });
                if (dialog.open() != null) {
                    selectedDirFileName = dialog.getFilterPath();
                    selectedFileNames = dialog.getFileNames();
                    String text = "";
                    for (String fileName : selectedFileNames) {
                        text += fileName + "\n";
                    }
                    selectedElementsText.setText(text);
                }
            }
        });
        importFromServerButton = new Button(importGroup, SWT.RADIO);
        importFromServerButton.setText(Localization.getString("button.importFromServer"));
        serverConnectorComposite = new WfeServerConnectorComposite(importGroup, getDataImporter(), new WfeServerConnectorSynchronizationCallback() {

            @Override
            public void onCompleted() {
                updateServerDataViewer(getServerDataViewerInput());
            }

            @Override
            public void onFailed() {
                updateServerDataViewer(null);
            }
            
        });
        createServerDataViewer(importGroup);
        setControl(pageControl);
        onImportModeChanged();
    }

    private void onImportModeChanged() {
        boolean fromFile = importFromFileButton.getSelection();
        selectedElementsText.setEnabled(fromFile);
        selectParsButton.setEnabled(fromFile);
        serverConnectorComposite.setEnabled(!fromFile);
        serverDataViewer.getControl().setEnabled(!fromFile);
        if (fromFile) {
            updateServerDataViewer(null);
        } else {
            updateServerDataViewer(getServerDataViewerInput());
        }
    }

    protected abstract List<? extends IContainer> getProjectDataViewerInput();

    protected LabelProvider getProjectDataViewerLabelProvider() {
        return new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((IContainer) element).getName();
            }
        };
    }

    protected abstract Object getServerDataViewerInput();

    private void createServerDataViewer(Composite parent) {
        serverDataViewer = new TreeViewer(parent);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        serverDataViewer.getControl().setLayoutData(gridData);
        serverDataViewer.setContentProvider(getServerDataViewerContentProvider());
        serverDataViewer.setLabelProvider(new BotStationLabelProvider());
        serverDataViewer.setInput(new Object());
    }

    private void updateServerDataViewer(Object data) {
        serverDataViewer.setInput(data);
        serverDataViewer.refresh(true);
    }

    public boolean performFinish() {
        try {
            String[] processNames;
            InputStream[] parInputStreams;
            boolean fromFile = importFromFileButton.getSelection();
            if (fromFile) {
                if (selectedDirFileName == null) {
                    throw new Exception(Localization.getString("error.selectValidFile"));
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
                    throw new Exception(Localization.getString("ImportBotElementWizardPage.error.empty.serverViewer.selection"));
                }
                processNames = new String[defSelections.size()];
                parInputStreams = new InputStream[defSelections.size()];
                for (int i = 0; i < processNames.length; i++) {
                    TreePath treePath = defSelections.get(i);
                    Object obj = treePath.getLastSegment();
                    if (obj instanceof BotStation) {
                        BotStation botStation = (BotStation) obj;
                        processNames[i] = botStation.getName();
                        byte[] par = ((WfeServerBotStationImporter) getDataImporter()).getBotStationFile(botStation);
                        parInputStreams[i] = new ByteArrayInputStream(par);
                    } else if (obj instanceof Bot) {
                        Bot bot = (Bot) obj;
                        processNames[i] = bot.getUsername();
                        byte[] par = ((WfeServerBotImporter) getDataImporter()).getBotFile(bot);
                        parInputStreams[i] = new ByteArrayInputStream(par);
                    } else if (obj instanceof BotTask) {
                        BotTask botTask = (BotTask) obj;
                        processNames[i] = botTask.getName();
                        byte[] par = ((WfeServerBotImporter) getDataImporter()).getBotTaskFile((Bot) treePath.getFirstSegment(), botTask.getName());
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

    protected abstract String getFilterExtension();

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

    protected abstract WfeServerConnectorDataImporter<?> getDataImporter();

    protected abstract ITreeContentProvider getServerDataViewerContentProvider();
}
