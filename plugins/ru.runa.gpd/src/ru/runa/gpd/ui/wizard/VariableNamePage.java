package ru.runa.gpd.ui.wizard;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.VariableNameChecker;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Objects;

public class VariableNamePage extends ContentWizardPage {
    private final VariableContainer variableContainer;
    private final String initialVariableName;
    private String variableName;
    private String variableDesc;
    private Text scriptingNameField;
    private String scriptingVariableName;

    public VariableNamePage(VariableContainer variableContainer, Variable variable) {
        this.variableContainer = variableContainer;
        if (variable != null && variable.getName() != null) {
            this.initialVariableName = variable.getName();
            this.variableName = variable.getName();
            this.scriptingVariableName = variable.getScriptingName();
        } else {
            initialVariableName = null;
            List<String> variableNames = VariableUtils.getVariableNames(variableContainer.getVariables(false, true));
            int runner = 1;
            while (true) {
                String candidate = Localization.getString("default.variable.name") + runner;
                if (!variableNames.contains(candidate)) {
                    this.variableName = candidate;
                    break;
                }
                runner++;
            }
            this.scriptingVariableName = VariableUtils.generateNameForScripting(variableContainer, variableName, variable);
        }
        this.variableDesc = variable != null && variable.getDescription() != null ? variable.getDescription() : "";
    }

    @Override
    protected int getGridLayoutColumns() {
        return 1;
    }

    @Override
    protected void createContent(Composite composite) {
        final Text nameField = new Text(composite, SWT.BORDER);
        nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameField.setText(variableName);
        nameField.addKeyListener(new VariableNameChecker());
        nameField.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                variableName = nameField.getText();
                scriptingVariableName = VariableUtils.generateNameForScripting(variableContainer, variableName, null);
                verifyContentIsValid();
                scriptingNameField.setText(scriptingVariableName);
            }
        });
        Label label = new Label(composite, SWT.NONE);
        label.setText(Localization.getString("VariableNamePage.scriptingName.label"));
        scriptingNameField = new Text(composite, SWT.BORDER);
        scriptingNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scriptingNameField.setEditable(false);
        scriptingNameField.setText(scriptingVariableName);
        label = new Label(composite, SWT.NONE);
        label.setText(Localization.getString("VariableNamePage.description.label"));
        final Text descriptionField = new Text(composite, SWT.BORDER);
        descriptionField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        descriptionField.setText(variableDesc);
        descriptionField.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                variableDesc = descriptionField.getText();
            }
        });
        nameField.setFocus();
        nameField.selectAll();
    }

    @Override
    protected void verifyContentIsValid() {
        if (variableName.length() == 0) {
            setErrorMessage(Localization.getString("VariableNamePage.error.empty"));
        } else if (!Objects.equal(initialVariableName, variableName) && 
                VariableUtils.getVariableNames(variableContainer.getVariables(false, true)).contains(variableName)) {
            setErrorMessage(Localization.getString("VariableNamePage.error.duplicated"));
        } else if (!VariableNameChecker.isValid(variableName)) {
            setErrorMessage(Localization.getString("VariableNamePage.error.invalidName"));
        } else {
            setErrorMessage(null);
        }
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableDesc() {
        return variableDesc;
    }
    
    public String getScriptingVariableName() {
        return scriptingVariableName;
    }
    
    public void setScriptingVariableName(String ScriptingName) {
        this.scriptingVariableName = ScriptingName;
    }
    
    public void setVariableName(String Name) {
        this.variableName = Name;
    }
}
