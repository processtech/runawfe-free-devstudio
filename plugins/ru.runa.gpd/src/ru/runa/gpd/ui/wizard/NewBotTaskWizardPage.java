package ru.runa.gpd.ui.wizard;

import java.lang.String;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.resources.IProject;
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

public class NewBotTaskWizardPage extends WizardPage {
    private Combo botStationCombo;
    private Combo botCombo;
    private Text nameText;
    private String startName;
    private final IStructuredSelection selection;

    public NewBotTaskWizardPage(IStructuredSelection selection) {
        super(Localization.getString("NewBotTaskWizardPage.page.name"));
        setTitle(Localization.getString("NewBotTaskWizardPage.page.title"));
        setDescription(Localization.getString("NewBotTaskWizardPage.page.description"));
        this.selection = selection;
    }

    public NewBotTaskWizardPage(IStructuredSelection selection, String startName) {
        super(Localization.getString("NewBotTaskWizardPage.page.name"));
        setTitle(Localization.getString("NewBotTaskWizardPage.page.title"));
        setDescription(Localization.getString("NewBotTaskWizardPage.page.description"));
        this.selection = selection;
        this.startName = startName;
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
        createBotField(composite);
        createNameField(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
        nameText.setFocus();
    }

    private void createBotStationField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("NewBotTaskWizardPage.botstation.name"));
        botStationCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (String botName : BotCache.getAllBotStationNames()) {
            botStationCombo.add(botName);
        }
        botStationCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final IProject botStationProject = getInitialBotStationElement(selection);
        if (botStationProject != null) {
            botStationCombo.setText(botStationProject.getName());
        }
        botStationCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                NewBotTaskWizardPage.this.fillBotCombo();
                verifyContentsValid();
            }
        });
    }

    private IProject getInitialBotStationElement(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;
                IFolder botFolder = (IFolder) adaptable.getAdapter(IFolder.class);
                return IOUtils.getBotStationProjectForBotFolder(botFolder);
            }
        }
        return null;
    }

    private void createBotField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("NewBotTaskWizardPage.bot.name"));
        botCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        fillBotCombo();
        botCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        IFolder resource = getInitialBotElement(selection);
        if (resource != null) {
            botCombo.setText(resource.getName());
        }
        botCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verifyContentsValid();
            }
        });
    }

    private IFolder getInitialBotElement(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;
                IFolder folder = (IFolder) adaptable.getAdapter(IFolder.class);
                return folder;
            }
        }
        return null;
    }

    private void fillBotCombo() {
        botCombo.removeAll();
        for (String botName : BotCache.getBotNames(botStationCombo.getText())) {
            botCombo.add(botName);
        }
    }

    private void createNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("NewBotTaskWizardPage.bottask.name"));
        nameText = new Text(parent, SWT.BORDER);
        if (startName != null && startName.length() > 0) {
            nameText.setText(startName);
        }
        nameText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void verifyContentsValid() {
        if (isBotTaskNameEmpty()) {
            setErrorMessage(Localization.getString("error.no_bottask_name"));
            setPageComplete(false);
        } else if (!isBotTaskNameValid()) {
            setErrorMessage(Localization.getString("error.bottask_name_not_valid"));
            setPageComplete(false);
        } else if (botCombo.getText().isEmpty()) {
            setPageComplete(false);
        } else if (isBotFolderContainsBotTask()) {
            setErrorMessage(Localization.getString("error.bottask_already_exists"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    public String getBotTaskName() {
        if (nameText == null) {
            return ""; //$NON-NLS-1$
        }
        return nameText.getText().trim();
    }

    public IFolder getBotFolder() {
        IProject botStationProject = IOUtils.getBotStationProject(botStationCombo.getText());
        if (botStationProject == null) {
            return null;
        }

        return IOUtils.getBotFolder(botStationProject, botCombo.getText());
    }

    private boolean isBotFolderContainsBotTask() {
        IFolder botFolder = getBotFolder();
        if (botFolder != null) {
            return botFolder.getFile(getBotTaskName()).exists();
        }
        return false;
    }

    private boolean isBotTaskNameEmpty() {
        return nameText.getText().length() == 0;
    }

    private boolean isBotTaskNameValid() {
        return ResourcesPlugin.getWorkspace().validateName(nameText.getText(), IResource.FILE).isOK();
    }
}
