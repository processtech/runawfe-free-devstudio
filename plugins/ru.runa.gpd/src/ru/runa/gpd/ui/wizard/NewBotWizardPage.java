package ru.runa.gpd.ui.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.gef.EditPart;
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

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.util.IOUtils;

public class NewBotWizardPage extends WizardPage {
    private Combo botStationCombo;
    private Text nameText;
    private final IStructuredSelection selection;

    public NewBotWizardPage(IStructuredSelection selection) {
        super(Localization.getString("NewBotWizardPage.page.name"));
        setTitle(Localization.getString("NewBotWizardPage.page.title"));
        setDescription(Localization.getString("NewBotWizardPage.page.description"));
        this.selection = selection;
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
        createBotStationField(composite);
        createNameField(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
        nameText.setFocus();
    }

    private void createBotStationField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("NewBotWizardPage.botstation.name"));
        botStationCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (String name : BotCache.getAllBotStationNames()) {
            botStationCombo.add(name);
        }
        botStationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        IProject project = getInitialBotStationElement(selection);
        if (project != null) {
            botStationCombo.setText(project.getName());
        }
        botStationCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verifyContentsValid();
            }
        });
    }

    private IProject getInitialBotStationElement(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof EditPart) {
                IFile file = IOUtils.getCurrentFile();
                return file == null ? null : file.getProject();
            }
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;
                IResource resource = (IResource) adaptable.getAdapter(IResource.class);
                if (resource != null) {
                    return resource.getProject();
                }
            }
        }
        return null;
    }

    private void createNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("NewBotWizardPage.bot.name"));
        nameText = new Text(parent, SWT.BORDER);
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void verifyContentsValid() {
        if (isBotNameEmpty()) {
            setErrorMessage(Localization.getString("error.no_bot_name"));
            setPageComplete(false);
        } else if (!isBotNameValid()) {
            setErrorMessage(Localization.getString("error.bot_name_not_valid"));
            setPageComplete(false);
        } else if (botExists()) {
            setErrorMessage(Localization.getString("error.bot_already_exists"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    public String getBotName() {
        if (nameText == null) {
            return ""; //$NON-NLS-1$
        }
        return nameText.getText().trim();
    }

    private boolean isBotNameEmpty() {
        return nameText.getText().length() == 0;
    }

    private boolean botExists() {
        return BotCache.getAllBotNames().contains(getBotName());
    }

    private boolean isBotNameValid() {
        return ResourcesPlugin.getWorkspace().validateName(nameText.getText(), IResource.FOLDER).isOK();
    }

    private IPath getBotFolderPath() {
        return new Path(botStationCombo.getText()).append("/src/botstation/");
    }

    public IFolder getBotFolder() {
        IPath path = getBotFolderPath().append(getBotName());
        return ResourcesPlugin.getWorkspace().getRoot().getFolder(path);
    }
}
