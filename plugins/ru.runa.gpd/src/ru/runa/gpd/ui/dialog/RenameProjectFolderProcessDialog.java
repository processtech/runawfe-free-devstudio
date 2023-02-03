package ru.runa.gpd.ui.dialog;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.FileNameChecker;
import ru.runa.gpd.util.IOUtils;

public class RenameProjectFolderProcessDialog extends Dialog {

    private String name;
    private IProject project;    
    private IFolder folder;
    private ProcessDefinition definition;

    public RenameProjectFolderProcessDialog(IProject project) {
        super(Display.getDefault().getActiveShell());
        this.project = project;
    }

    public RenameProjectFolderProcessDialog(IFolder folder) {
        super(Display.getDefault().getActiveShell());
        this.folder = folder;
    }

    public RenameProjectFolderProcessDialog(ProcessDefinition definition) {
        super(Display.getDefault().getActiveShell());
        this.definition = definition;
    }

    public void setName(String name) {
		this.name = name;
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        final Label labelTitle = new Label(area, SWT.NO_BACKGROUND);
        final GridData labelData = new GridData();
        labelTitle.setLayoutData(labelData);
        labelTitle.setText(Localization.getString("button.rename"));

        final Composite composite = new Composite(area, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        GridData nameData = new GridData();
        composite.setLayoutData(nameData);

        Label labelName = new Label(composite, SWT.NONE);
        labelName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelName.setText(Localization.getString("property.name") + ":");
        final Text nameField = new Text(composite, SWT.BORDER);
        GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
        nameTextData.minimumWidth = 200;
        nameField.setText(name);
        nameField.setLayoutData(nameTextData);
        nameField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                name = nameField.getText();
                updateButtons();
            }
        });

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    private void updateButtons() {
    	IWorkspace workspace = ResourcesPlugin.getWorkspace();
        boolean allowCreation = FileNameChecker.isValid(name);
        if (project != null) {
            IStatus nameStatus = workspace.validateName(name,IResource.PROJECT);
            if (name.isEmpty() || !nameStatus.isOK() || ResourcesPlugin.getWorkspace().getRoot().getProject(name).exists()) {
                allowCreation = false;
            }
        } else if (folder != null) {
            if (folder.getFullPath().lastSegment().startsWith(".")) {
                allowCreation &= !IOUtils.isChildFolderExists(folder.getParent(), "." + name);
            } else {
                allowCreation &= !IOUtils.isChildFolderExists(folder.getParent(), name);
            }
        } else if (definition != null) {
            allowCreation &= definition.getEmbeddedSubprocessByName(name) == null;
            allowCreation &= !definition.getName().equals(name);
            folder = (IFolder) definition.getFile().getParent();
            if (folder.getFullPath().lastSegment().startsWith(".")) {
                allowCreation &= !IOUtils.isChildFolderExists(folder.getParent(), "." + name);
            } else {
                allowCreation &= !IOUtils.isChildFolderExists(folder.getParent(), name);
            }
        }
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (project != null) {
            newShell.setText(Localization.getString("RenameProjectDialog.title"));
        } else if (folder != null) {
            newShell.setText(Localization.getString("RenameFolderDialog.title"));
        } else if (definition != null) {
            newShell.setText(Localization.getString("RenameProcessDefinitionDialog.title"));
        }
    }

    public String getName() {
        return name;
    }
}
