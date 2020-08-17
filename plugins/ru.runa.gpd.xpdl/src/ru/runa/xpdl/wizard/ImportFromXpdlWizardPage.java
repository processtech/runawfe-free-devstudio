package ru.runa.xpdl.wizard;

import java.io.File;
import java.util.Arrays;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ui.wizard.ImportWizardPage;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.xpdl.convertor.XpdlImporter;
import ru.runa.xpdl.resource.Messages;

public class ImportFromXpdlWizardPage extends ImportWizardPage {
    private Composite fileSelectionArea;
    private FileFieldEditor editor;
    private Button importForDefaultSwimLaneButton;
    private Button importForParticipantSwimLaneButton;
    private Text runaUsersGroupName;
    private boolean useDefaultSwimlane = false;

    public ImportFromXpdlWizardPage(IStructuredSelection selection) {
        super(ImportFromXpdlWizardPage.class, selection);
        setTitle(Messages.getString("ImportXPDLWizardPage.page.title"));
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        createProjectsGroup(sashForm, IOUtils.getAllProcessContainers(), new LabelProvider() {
            @Override
            public String getText(Object element) {
                return IOUtils.getProcessContainerName((IContainer) element);
            }
        });
        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        importForDefaultSwimLaneButton = new Button(importGroup, SWT.RADIO);
        importForDefaultSwimLaneButton.setText(Messages.getString("XPDLConnector.UseOneRoleForAllActivities"));
        importForDefaultSwimLaneButton.setSelection(false);
        importForDefaultSwimLaneButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useDefaultSwimlane = true;
            }
        });
        importForParticipantSwimLaneButton = new Button(importGroup, SWT.RADIO);
        importForParticipantSwimLaneButton.setText(Messages.getString("XPDLConnector.SetSwimlanesAsParticipants"));
        importForParticipantSwimLaneButton.setSelection(true);
        importForParticipantSwimLaneButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                useDefaultSwimlane = false;
            }
        });
        Label label = new Label(importGroup, SWT.NONE);
        label.setText(Messages.getString("XPDLConnector.RunaUsersGroupName"));
        runaUsersGroupName = new Text(importGroup, SWT.BORDER);
        runaUsersGroupName.setText("Administrators");
        runaUsersGroupName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fileSelectionArea = new Composite(importGroup, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionArea.setLayoutData(fileSelectionData);
        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 3;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);
        editor = new FileFieldEditor("fileSelect", Messages.getString("ImportXPDLWizardPage.page.title"), fileSelectionArea);
        editor.setFileExtensions(new String[] { "*.xpdl" });
        setControl(pageControl);
    }

    public boolean performFinish() {
        try {
            IContainer container = getSelectedProject();
            String xpdlFileName = editor.getStringValue();
            if (xpdlFileName == null || !new File(xpdlFileName).exists()) {
                throw new Exception(Messages.getString("ImportXPDLWizardPage.error.selectFile"));
            }
            String runaGroupName = this.runaUsersGroupName.getText();
            new XpdlImporter().parseXPDLFile(container.getLocation().toFile(), xpdlFileName, useDefaultSwimlane, runaGroupName);
            WorkspaceOperations.refreshResources(Arrays.asList((IResource) container));
            return true;
        } catch (Exception e) {
            String s = e.getMessage();
            if (s == null || s.length() == 0) {
                s = e.getClass().getName();
            }
            setErrorMessage(s);
            PluginLogger.logErrorWithoutDialog("import from xpdl", e);
            return false;
        }
    }
}
