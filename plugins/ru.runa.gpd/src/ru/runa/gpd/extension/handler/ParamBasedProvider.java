package ru.runa.gpd.extension.handler;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class ParamBasedProvider extends DelegableProvider {
    protected abstract ParamDefConfig getParamConfig(Delegable delegable);

    protected ImageDescriptor getLogo() {
        return null;
    }

    @Override
    public String showConfigurationDialog(Delegable delegable) {
        ParamDefConfig config = getParamConfig(delegable);
        return showConfigurationDialog(delegable, config, getLogo());
    }

    public static String showConfigurationDialog(Delegable delegable, ParamDefConfig config, ImageDescriptor logo) {
        ConfigurationWizardPage page = new ConfigurationWizardPage(delegable, config, logo);
        final ConfigurationWizard wizard = new ConfigurationWizard(page);
        CompactWizardDialog dialog = new CompactWizardDialog(wizard) {
            @Override
            protected void createButtonsForButtonBar(Composite parent) {
                Button copyButton = createButton(parent, 197, Localization.getString("button.copy"), false);
                copyButton.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Clipboard clipboard = new Clipboard(Display.getCurrent());
                        clipboard.setContents(new String[] { wizard.getWizardPage().getConfiguration() },
                                new Transfer[] { TextTransfer.getInstance() });
                        clipboard.dispose();
                    }
                });
                super.createButtonsForButtonBar(parent);
            }
        };
        if (dialog.open() == IDialogConstants.OK_ID) {
            return wizard.getConfiguration();
        }
        return null;
    }

    @Override
    public List<String> getUsedVariableNames(Delegable delegable) {
        String configuration = delegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return Lists.newArrayList();
        }
        List<String> result = Lists.newArrayList();
        ParamDefConfig paramDefConfig = getParamConfig(delegable);
        Map<String, String> props = paramDefConfig.parseConfiguration(configuration);
        for (ParamDefGroup group : paramDefConfig.getGroups()) {
            for (ParamDef paramDef : group.getParameters()) {
                String value = props.get(paramDef.getName());
                if (paramDef.isUseVariable() && value != null) {
                    result.add(value);
                }
            }
        }
        return result;
    }

    @Override
    public String getConfigurationOnVariableRename(Delegable delegable, Variable currentVariable, Variable previewVariable) {
        String configuration = delegable.getDelegationConfiguration();
        if (Strings.isNullOrEmpty(configuration)) {
            return configuration;
        }
        ParamDefConfig paramDefConfig = getParamConfig(delegable);
        Map<String, String> properties = paramDefConfig.parseConfiguration(configuration);
        for (ParamDefGroup group : paramDefConfig.getGroups()) {
            for (ParamDef paramDef : group.getParameters()) {
                String value = properties.get(paramDef.getName());
                if (paramDef.isUseVariable() && Objects.equal(value, currentVariable.getName())) {
                    properties.put(paramDef.getName(), previewVariable.getName());
                }
            }
        }
        List<String> variableNames = delegable.getVariableNames(true);
        variableNames.add(previewVariable.getName());
        return paramDefConfig.toConfiguration(variableNames, properties);
    }

    @Override
    public boolean validateValue(Delegable delegable, List<ValidationError> errors) {
        return getParamConfig(delegable).validate(delegable, errors);
    }

    public static class ConfigurationWizard extends Wizard {
        private final ConfigurationWizardPage wizardPage;
        private String configuration;

        public ConfigurationWizard(ConfigurationWizardPage wizardPage) {
            this.wizardPage = wizardPage;
            setWindowTitle(Localization.getString("property.delegation.configuration"));
        }

        @Override
        public void addPages() {
            addPage(wizardPage);
        }

        public String getConfiguration() {
            return configuration;
        }

        public ConfigurationWizardPage getWizardPage() {
            return wizardPage;
        }

        @Override
        public boolean performFinish() {
            configuration = wizardPage.getConfiguration();
            return true;
        }
    }

    public static class ConfigurationWizardPage extends WizardPage {
        private ParamDefComposite paramDefComposite;
        private final ParamDefConfig config;
        private final Delegable delegable;
        private final Map<String, String> properties;

        protected ConfigurationWizardPage(Delegable delegable, ParamDefConfig config, ImageDescriptor logo) {
            super("config", LocalizationRegistry.getLabel(delegable.getDelegationClassName()), logo);
            this.delegable = delegable;
            this.properties = config.parseConfiguration(delegable.getDelegationConfiguration());
            this.config = config;
        }

        @Override
        public void createControl(Composite parent) {
            ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
            scrolledComposite.setExpandHorizontal(true);
            scrolledComposite.setExpandVertical(true);
            scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            paramDefComposite = new ParamDefComposite(scrolledComposite, delegable, config, properties);
            paramDefComposite.createUI();
            scrolledComposite.setMinSize(paramDefComposite.computeSize(paramDefComposite.getSize().x, SWT.DEFAULT));
            scrolledComposite.setContent(paramDefComposite);
            setControl(scrolledComposite);
        }

        public String getConfiguration() {
            Map<String, String> properties = paramDefComposite.readUserInput();
            return config.toConfiguration(delegable.getVariableNames(true), properties);
        }
    }
}
