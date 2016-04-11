package ru.runa.gpd.ui.wizard;

import java.net.URI;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WorkingSetGroup;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import ru.runa.gpd.Localization;

public class NewBotStationWizardPage extends WizardPage {
    // initial value stores
    private String initialProjectFieldValue;
    // widgets
    private Text projectNameField;
    private Text rmiAddressField;
    private final IWorkspaceRoot workspaceRoot;
    private Listener nameModifyListener = new Listener() {
        @Override
        public void handleEvent(Event e) {
            setLocationForSelection();
            boolean valid = validatePage();
            setPageComplete(valid);
        }
    };
    private ProjectContentsLocationArea locationArea;
    private WorkingSetGroup workingSetGroup;
    // constants
    private static final int SIZING_TEXT_FIELD_WIDTH = 250;

    /**
     * Creates a new project creation wizard page.
     *
     * @param pageName the name of this page
     */
    public NewBotStationWizardPage(String pageName) {
        super(pageName);
        setTitle(Localization.getString("NewBotStationWizardPage.page.title"));
        setDescription(Localization.getString("NewBotStationWizardPage.page.description"));
        this.workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        setPageComplete(false);
    }

    /** (non-Javadoc)
     * Method declared on IDialogPage.
     */
    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        initializeDialogUnits(parent);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createProjectNameGroup(composite);
        createRmiAddressGroup(composite);
        Composite c = new Composite(composite, SWT.NONE);
        c.setLayout(new GridLayout());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        c.setLayoutData(data);
        locationArea = new ProjectContentsLocationArea(getErrorReporter(), c);
        if (initialProjectFieldValue != null) {
            locationArea.updateProjectName(initialProjectFieldValue);
        }
        // Scale the button based on the rest of the dialog
        setButtonLayoutData(locationArea.getBrowseButton());
        setPageComplete(validatePage());
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    /**
     * Create a working set group for this page. This method can only be called
     * once.
     * 
     * @param composite
     *            the composite in which to create the group
     * @param selection
     *            the current workbench selection
     * @param supportedWorkingSetTypes
     *            an array of working set type IDs that will restrict what types
     *            of working sets can be chosen in this group
     * @return the created group. If this method has been called previously the
     *         original group will be returned.
     * @since 3.4
     */
    public WorkingSetGroup createWorkingSetGroup(Composite composite, IStructuredSelection selection, String[] supportedWorkingSetTypes) {
        if (workingSetGroup != null) {
            return workingSetGroup;
        }
        workingSetGroup = new WorkingSetGroup(composite, selection, supportedWorkingSetTypes);
        return workingSetGroup;
    }

    /**
     * Get an error reporter for the receiver.
     * @return IErrorMessageReporter
     */
    private IErrorMessageReporter getErrorReporter() {
        return new IErrorMessageReporter() {
            /* (non-Javadoc)
             * @see org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter#reportError(java.lang.String)
             */
            @Override
            public void reportError(String errorMessage, boolean infoOnly) {
                if (infoOnly) {
                    setMessage(errorMessage, IStatus.INFO);
                    setErrorMessage(null);
                } else {
                    setErrorMessage(errorMessage);
                }
                boolean valid = errorMessage == null;
                if (valid) {
                    valid = validatePage();
                }
                setPageComplete(valid);
            }
        };
    }

    /**
     * Creates the project name specification controls.
     *
     * @param parent the parent composite
     */
    private void createProjectNameGroup(Composite parent) {
        // new project label
        Label projectLabel = new Label(parent, SWT.NONE);
        projectLabel.setText(Localization.getString("NewBotStationWizardPage.page.name"));
        projectLabel.setFont(parent.getFont());
        // new project name entry field
        projectNameField = new Text(parent, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = SIZING_TEXT_FIELD_WIDTH;
        projectNameField.setLayoutData(data);
        projectNameField.setFont(parent.getFont());
        // Set the initial value first before listener
        // to avoid handling an event during the creation.
        if (initialProjectFieldValue != null) {
            projectNameField.setText(initialProjectFieldValue);
        }
        projectNameField.addListener(SWT.Modify, nameModifyListener);
    }

    private void createRmiAddressGroup(Composite parent) {
        Label projectLabel = new Label(parent, SWT.NONE);
        projectLabel.setText(Localization.getString("NewBotStationWizardPage.page.address"));
        projectLabel.setFont(parent.getFont());
        rmiAddressField = new Text(parent, SWT.BORDER);
        GridData addressData = new GridData(GridData.FILL_HORIZONTAL);
        addressData.widthHint = SIZING_TEXT_FIELD_WIDTH;
        rmiAddressField.setLayoutData(addressData);
        rmiAddressField.setFont(parent.getFont());
    }

    /**
     * Returns the current project location path as entered by 
     * the user, or its anticipated initial value.
     * Note that if the default has been returned the path
     * in a project description used to create a project
     * should not be set.
     *
     * @return the project location path or its anticipated initial value.
     */
    public IPath getLocationPath() {
        return new Path(locationArea.getProjectLocation());
    }

    /**
    /**
     * Returns the current project location URI as entered by 
     * the user, or <code>null</code> if a valid project location
     * has not been entered.
     *
     * @return the project location URI, or <code>null</code>
     * @since 3.2
     */
    public URI getLocationURI() {
        return locationArea.getProjectLocationURI();
    }

    /**
     * Creates a project resource handle for the current project name field
     * value. The project handle is created relative to the workspace root.
     * <p>
     * This method does not create the project resource; this is the
     * responsibility of <code>IProject::create</code> invoked by the new
     * project resource wizard.
     * </p>
     * 
     * @return the new project resource handle
     */
    public IProject getProjectHandle() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
    }

    /**
     * Returns the current project name as entered by the user, or its anticipated
     * initial value.
     *
     * @return the project name, its anticipated initial value, or <code>null</code>
     *   if no project name is known
     */
    public String getProjectName() {
        if (projectNameField == null) {
            return initialProjectFieldValue;
        }
        return getProjectNameFieldValue();
    }

    /**
     * Returns the value of the project name field
     * with leading and trailing spaces removed.
     * 
     * @return the project name in the field
     */
    private String getProjectNameFieldValue() {
        if (projectNameField == null) {
            return ""; //$NON-NLS-1$
        }
        return projectNameField.getText().trim();
    }

    public String getRmiAddress() {
        if (rmiAddressField == null) {
            return initialProjectFieldValue;
        }
        return getRmiAddressValue();
    }

    private String getRmiAddressValue() {
        if (rmiAddressField == null) {
            return ""; //$NON-NLS-1$
        }
        return rmiAddressField.getText().trim();
    }

    /**
     * Sets the initial project name that this page will use when
     * created. The name is ignored if the createControl(Composite)
     * method has already been called. Leading and trailing spaces
     * in the name are ignored.
     * Providing the name of an existing project will not necessarily 
     * cause the wizard to warn the user.  Callers of this method 
     * should first check if the project name passed already exists 
     * in the workspace.
     * 
     * @param name initial project name for this page
     * 
     * @see IWorkspace#validateName(String, int)
     * 
     */
    public void setInitialProjectName(String name) {
        if (name == null) {
            initialProjectFieldValue = null;
        } else {
            initialProjectFieldValue = name.trim();
            if (locationArea != null) {
                locationArea.updateProjectName(name.trim());
            }
        }
    }

    /**
     * Set the location to the default location if we are set to useDefaults.
     */
    void setLocationForSelection() {
        locationArea.updateProjectName(getProjectNameFieldValue());
    }

    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    protected boolean validatePage() {
        IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
        String projectFieldContents = getProjectNameFieldValue();
        if (projectFieldContents.equals("")) { //$NON-NLS-1$
            setErrorMessage(null);
            setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
            return false;
        }
        IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }
        IProject handle = getProjectHandle();
        if (handle.exists()) {
            setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
            return false;
        }
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectNameFieldValue());
        locationArea.setExistingProject(project);
        String validLocationMessage = locationArea.checkValidLocation();
        if (validLocationMessage != null) { // there is no destination location given
            setErrorMessage(validLocationMessage);
            return false;
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    /*
     * see @DialogPage.setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            projectNameField.setFocus();
        }
    }

    /**
     * Returns the useDefaults.
     * @return boolean
     */
    public boolean useDefaults() {
        return locationArea.isDefault();
    }

    /**
     * Return the selected working sets, if any. If this page is not configured
     * to interact with working sets this will be an empty array.
     * 
     * @return the selected working sets
     * @since 3.4
     */
    public IWorkingSet[] getSelectedWorkingSets() {
        return workingSetGroup == null ? new IWorkingSet[0] : workingSetGroup.getSelectedWorkingSets();
    }

    public IFolder getBotStationFolder() {
        IPath path = getProcessFolderPath().append(projectNameField.getText());
        return workspaceRoot.getFolder(path);
    }

    private IPath getProcessFolderPath() {
        return new Path(projectNameField.getText()).append("/src/botstation/");
    }
}
