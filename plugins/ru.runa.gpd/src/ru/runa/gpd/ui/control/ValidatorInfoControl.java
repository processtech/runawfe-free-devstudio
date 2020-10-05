package ru.runa.gpd.ui.control;

import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import ru.runa.gpd.Localization;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;

public abstract class ValidatorInfoControl extends Composite {
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
        errorMessageLabel.setText(Localization.getString("ValidatorInfoControl.ErrorMessage"));
        errorMessageText = new Text(this, SWT.BORDER);
        errorMessageText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        errorMessageText.setToolTipText(Localization.getString("ValidatorInfoControl.ErrorMessage"));
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
            errorMessageText.setData(new Object());
            errorMessageText.setText(config.getMessage());
            errorMessageText.setData(null);
            parametersComposite.clear();
            parametersComposite.build(definition, config.getParams());
        }
        setVisible(config != null);
    }

    protected boolean isConfiguring(Widget widget) {
        return widget.getData() != null;
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

    public static abstract class ParametersComposite extends Composite {

        public ParametersComposite(Composite parent, int style) {
            super(parent, style);
        }

        protected abstract void clear();

        protected abstract void build(ValidatorDefinition definition, Map<String, String> configParams);

        protected abstract void updateConfigParams(ValidatorDefinition definition, ValidatorConfig config);
    }

}
