package ru.runa.gpd.ui.wizard;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.IOUtils;

public class NewFolderWizardPage extends WizardPage {
    private Combo projectCombo;
    private Text folderText;
    private final IContainer initialSelection;
    private final List<IContainer> processContainers;

    public NewFolderWizardPage(IStructuredSelection selection) {
        super(Localization.getString("NewFolderWizardPage.page.name"));
        setTitle(Localization.getString("NewFolderWizardPage.page.title"));
        setDescription(Localization.getString("NewFolderWizardPage.page.description"));
        this.initialSelection = (IContainer) IOUtils.getProcessSelectionResource(selection);
        this.processContainers = IOUtils.getAllProcessContainers();
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 2;
        composite.setLayout(layout);
        createProjectField(composite);
        createFolderNameField(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
        folderText.setFocus();
    }

    private void createProjectField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.project"));
        projectCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (IContainer container : processContainers) {
            projectCombo.add(IOUtils.getProcessContainerName((IContainer) container));
        }
        projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (initialSelection != null) {
            projectCombo.setText(IOUtils.getProcessContainerName(initialSelection));
        }
        projectCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verifyContentsValid();
            }
        });
    }

    private void createFolderNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.folder_name"));
        folderText = new Text(parent, SWT.BORDER);
        folderText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        folderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }


    private void verifyContentsValid() {
        if (projectCombo.getText().length() == 0) {
            setErrorMessage(Localization.getString("error.choose_project"));
            setPageComplete(false);
        } else if (folderText.getText().length() == 0) {
            setErrorMessage(Localization.getString("error.no_folder_name"));
            setPageComplete(false);
        } else if (!ResourcesPlugin.getWorkspace().validateName(folderText.getText(), IResource.FOLDER).isOK()) {
            setErrorMessage(Localization.getString("error.folder_name_not_valid"));
            setPageComplete(false);
        } else if (getFolder().exists()) {
            setErrorMessage(Localization.getString("error.folder_already_exists"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    private String getFolderName() {
        return folderText.getText();
    }

    public IFolder getFolder() {
        IContainer container = processContainers.get(projectCombo.getSelectionIndex());
        return IOUtils.getProcessFolder(container, getFolderName());
    }
}
