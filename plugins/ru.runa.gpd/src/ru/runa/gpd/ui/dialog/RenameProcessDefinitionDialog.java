package ru.runa.gpd.ui.dialog;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
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
import ru.runa.gpd.util.IOUtils;

public class RenameProcessDefinitionDialog extends Dialog {
    private String name;
    private IFolder definitionFolder;
    private ProcessDefinition definition;

    public RenameProcessDefinitionDialog(IFolder definitionFolder) {
        super(Display.getDefault().getActiveShell());
        this.definitionFolder = definitionFolder;
    }

    public RenameProcessDefinitionDialog(ProcessDefinition definition) {
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
        boolean allowCreation = workspace.validateName(name, IResource.FOLDER).isOK();
        if (definitionFolder != null) {
            allowCreation &= !IOUtils.isChildFolderExists(definitionFolder.getParent(), name);
        } else if (definition != null) {
            allowCreation &= definition.getEmbeddedSubprocessByName(name) == null;
        }
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("RenameProcessDefinitionDialog.title"));
    }

    public String getName() {
        return name;
    }

}
