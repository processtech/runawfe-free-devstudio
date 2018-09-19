package ru.runa.gpd.ui.wizard;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidationUtil;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDialog;
import ru.runa.gpd.validation.ValidatorParser;

public class ValidatorWizard extends Wizard {
    protected FieldValidatorsWizardPage fieldValidatorsPage;
    protected GlobalValidatorsWizardPage globalValidatorsPage;
    private final IFile validationFile;
    private final FormNode formNode;
    private FormNodeValidation validation;

    public ValidatorWizard(IFile validationFile, FormNode formNode) {
        this.validationFile = validationFile;
        this.formNode = formNode;
        this.validation = ValidatorParser.parseValidation(validationFile);
        setWindowTitle(Localization.getString("ValidatorWizard.wizard.title"));
        setDefaultPageImageDescriptor(SharedImages.getImageDescriptor("/icons/FormValidation.png"));
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        if (!formNode.hasForm()) {
            ((ValidatorDialog) getContainer()).getResetToDefaultsButton().setEnabled(false);
        }
        ((ValidatorDialog) getContainer()).getResetToDefaultsButton().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                try {
                    validation = ValidationUtil.getInitialFormValidation(validationFile, formNode);
                    initPages();
                } catch (Exception e) {
                    PluginLogger.logError("Extracting variables from form error", e);
                }
            }
        });
    }

    private void initPages() {
        fieldValidatorsPage.init(validation);
        globalValidatorsPage.init(validation);
    }

    @Override
    public boolean performFinish() {
        // regenerate validator.xml
        fieldValidatorsPage.performFinish();
        globalValidatorsPage.performFinish();
        // remove configs with deleted variables
        Set<String> missedVariableNames = new HashSet<String>();
        missedVariableNames.addAll(validation.getVariableNames());
        missedVariableNames.removeAll(formNode.getVariableNames(true));
        for (String variableName : missedVariableNames) {
            validation.removeFieldConfigs(variableName);
        }
        ValidatorParser.writeValidation(validationFile, formNode, validation);
        return true;
    }

    @Override
    public void dispose() {
        if (validation.getVariableNames().isEmpty()) {
            try {
                ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
                    @Override
                    public void run(IProgressMonitor monitor) throws CoreException {
                        validationFile.delete(true, null);
                    }
                }, null);
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        }
        super.dispose();
    }

    @Override
    public void addPages() {
        fieldValidatorsPage = new FieldValidatorsWizardPage(formNode);
        globalValidatorsPage = new GlobalValidatorsWizardPage(formNode);
        initPages();
        addPage(fieldValidatorsPage);
        addPage(globalValidatorsPage);
    }

    public static abstract class ParametersComposite extends Composite {
        public ParametersComposite(Composite parent, int style) {
            super(parent, style);
        }

        protected abstract void clear();

        protected abstract void build(ValidatorDefinition definition, Map<String, String> configParams);

        protected abstract void updateConfigParams(ValidatorDefinition definition, ValidatorConfig config);
    }

    public static abstract class ValidatorInfoControl extends Composite {
        protected ValidatorDefinition definition;
        protected ValidatorConfig config;
        protected ParametersComposite parametersComposite;
        private Label descriptionLabel;
        protected Label errorMessageLabel;
        protected Text errorMessageText;
        protected Label descriptionForUserLabel;
        protected Text descriptionForUserText;

        public ValidatorInfoControl(Composite parent, boolean showDescription) {
            super(parent, SWT.BORDER);
            this.setLayout(new GridLayout(1, true));
            if (showDescription) {
                descriptionLabel = new Label(this, SWT.NONE);
                descriptionLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                descriptionLabel.setText("\n");
            }
            errorMessageLabel = new Label(this, SWT.NONE);
            errorMessageLabel.setText(Localization.getString("ValidatorsWizardPage.ErrorMessage"));
            errorMessageText = new Text(this, SWT.BORDER);
            errorMessageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            errorMessageText.setToolTipText(Localization.getString("ValidatorsWizardPage.ErrorMessage"));
        }

        protected abstract boolean enableUI(String variableName, ValidatorDefinition definition, ValidatorConfig config);

        public void setConfig(String variableName, ValidatorDefinition definition, ValidatorConfig config) {
            this.setEnabled(enableUI(variableName, definition, config));
            if (config != null) {
                saveConfig();
                this.config = config;
                this.definition = definition;
                if (descriptionLabel != null) {
                    descriptionLabel.setText(definition.getDescription());
                }
                errorMessageText.setText(config.getMessage());
                parametersComposite.clear();
                parametersComposite.build(definition, config.getParams());
                errorMessageText.setFocus();
            }
            setVisible(config != null);
        }

        public void saveConfig() {
            if (config != null) {
                // save input data to config
                config.setMessage(errorMessageText.getText());
                parametersComposite.updateConfigParams(definition, config);
            }
        }

        public ValidatorConfig getConfig() {
            return config;
        }
    }

}