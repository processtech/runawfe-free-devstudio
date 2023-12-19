package ru.runa.gpd.ui.wizard;

import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.Dialog;
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
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.FileNameChecker;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class CopyProcessDefinitionWizardPage extends WizardPage {
    private Combo projectCombo;
    private Text processText;
    private Combo languageCombo;
    private Combo swimlaneDisplayCombo;
    private final IFolder sourceProcessFolder;
    private final ProcessDefinition sourceDefinition;
    private final List<IContainer> processContainers;

    public CopyProcessDefinitionWizardPage(IFolder sourceProcessFolder) {
        super(Localization.getString("CopyProcessDefinitionWizardPage.page.name"));
        this.sourceProcessFolder = sourceProcessFolder;
        setTitle(Localization.getString("CopyProcessDefinitionWizardPage.page.title"));
        setDescription(Localization.getString("CopyProcessDefinitionWizardPage.page.description"));
        IFile definitionFile = IOUtils.getProcessDefinitionFile(sourceProcessFolder);
        sourceDefinition = ProcessCache.getProcessDefinition(definitionFile);
        if (sourceDefinition == null) {
            throw new NullPointerException("Process definition is null");
        }
        this.processContainers = IOUtils.getAllProcessContainers();
    }

    public IFolder getSourceProcessFolder() {
        return sourceProcessFolder;
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
        createProcessNameField(composite);
        createJpdlVersionCombo(composite);
        createBpmnDisplaySwimlaneCombo(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        setPageComplete(false);
        processText.setFocus();
    }

    private void createProjectField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.project"));
        projectCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (IContainer container : processContainers) {
            projectCombo.add(IOUtils.getProcessContainerName(container));
        }
        projectCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                verifyContentsValid();
            }
        });
    }

    private void createProcessNameField(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.process_name"));
        processText = new Text(parent, SWT.BORDER);
        processText.setText(sourceProcessFolder.getName());
        processText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                verifyContentsValid();
            }
        });
        processText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createJpdlVersionCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.language"));
        languageCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        languageCombo.setEnabled(false);
        for (Language language : Language.values()) {
            languageCombo.add(language.name());
        }
        languageCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        languageCombo.setText(sourceDefinition.getLanguage().name());
    }

    private void createBpmnDisplaySwimlaneCombo(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("label.bpmn.display.swimlane"));
        swimlaneDisplayCombo = new Combo(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
        for (int i = 0; i < SwimlaneDisplayMode.values().length; i++) {
            SwimlaneDisplayMode mode = SwimlaneDisplayMode.values()[i];
            swimlaneDisplayCombo.add(mode.getLabel());
            if (sourceDefinition.getSwimlaneDisplayMode().equals(mode)) {
                swimlaneDisplayCombo.select(i);
            }
        }
        swimlaneDisplayCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void verifyContentsValid() {
        if (projectCombo.getText().length() == 0) {
            setErrorMessage(Localization.getString("error.choose_project"));
            setPageComplete(false);
        } else if (processText.getText().length() == 0) {
            setErrorMessage(Localization.getString("error.no_process_name"));
            setPageComplete(false);
        } else if (!FileNameChecker.isValid(processText.getText())) {
            setErrorMessage(Localization.getString("error.process_name_not_valid"));
            setPageComplete(false);
        } else if (getTargetProcessFolder().exists()) {
            setErrorMessage(Localization.getString("error.process_already_exists"));
            setPageComplete(false);
        } else {
            setErrorMessage(null);
            setPageComplete(true);
        }
    }

    public String getProcessName() {
        return processText.getText();
    }

    public Language getLanguage() {
        return Language.valueOf(languageCombo.getText());
    }

    public SwimlaneDisplayMode getSwimlaneDisplayMode() {
        return SwimlaneDisplayMode.values()[swimlaneDisplayCombo.getSelectionIndex()];
    }

    public IFolder getTargetProcessFolder() {
        IContainer container = processContainers.get(projectCombo.getSelectionIndex());
        return IOUtils.getProcessFolder(container, getProcessName());
    }

}
