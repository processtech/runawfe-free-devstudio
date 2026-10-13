package ru.runa.gpd.ui.dialog;

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
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableStorageKind;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.VariableNameChecker;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;

public class UpdateVariableNameDialog extends Dialog {
    private final VariableContainer variableContainer;
    private final Variable variable;
    private final boolean isRename;
    private Text scriptingNameField;
    private String scriptingName;
    private String name;
    private String redmineFieldName;

    public UpdateVariableNameDialog(VariableContainer variableContainer, Variable variable) {
        this(variableContainer, variable, false);
    }

    public UpdateVariableNameDialog(VariableContainer variableContainer, Variable variable, boolean isRename) {
        super(Display.getDefault().getActiveShell());
        this.variableContainer = variableContainer;
        this.variable = variable;
        this.isRename = isRename;
        this.name = variable.getName();
        this.redmineFieldName = variable.getRedmineFieldName();
        if (variable.getScriptingName() != null) {
            this.scriptingName = variable.getScriptingName();
        } else {
            this.scriptingName = VariableUtils.generateNameForScripting(variableContainer, name, variable);
        }
    }

    private boolean isRedmineAttribute() {
        return variableContainer instanceof VariableUserType
                && ((VariableUserType) variableContainer).getReferenceStorage() == VariableStorageKind.REDMINE;
    }

    public UpdateVariableNameDialog(Variable variable) {
        this(variable.getProcessDefinition(), variable);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);

        Label labelTitle = new Label(area, SWT.NO_BACKGROUND);
        GridData labelData = new GridData();
        labelTitle.setLayoutData(labelData);
        labelTitle.setText(Localization.getString("VariableWizard.update.message"));

        Composite composite = new Composite(area, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
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
        nameField.setLayoutData(nameTextData);
        nameField.setText(name);
        nameField.addKeyListener(new VariableNameChecker());
        nameField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                name = nameField.getText();

                scriptingName = VariableUtils.generateNameForScripting(variableContainer, name, variable);
                scriptingNameField.setText(scriptingName);

                updateButtons();
            }
        });
        nameField.selectAll();

        new Label(composite, SWT.NONE);

        scriptingNameField = new Text(composite, SWT.BORDER);
        scriptingNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scriptingNameField.setEditable(false);
        scriptingNameField.setText(scriptingName);

        if (isRedmineAttribute()) {
            Label redmineLabel = new Label(composite, SWT.NONE);
            redmineLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
            redmineLabel.setText(Localization.getString("Variable.property.redmineFieldName") + ":");

            final Text redmineField = new Text(composite, SWT.BORDER);
            redmineField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (redmineFieldName != null) {
                redmineField.setText(redmineFieldName);
            }
            redmineField.setMessage(variable.getName());
            redmineField.addModifyListener(ev -> {
                redmineFieldName = redmineField.getText();
                updateButtons();
            });
        }

        Text saveAllEditorsLabel = new Text(composite, SWT.MULTI | SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        saveAllEditorsLabel.setLayoutData(gridData);
        saveAllEditorsLabel.setText(Localization.getString("warning.allEditorsWillBeSaved"));

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setText(Localization.getString("button.proceed"));
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    private void updateButtons() {
        boolean nameValid = !Strings.isNullOrEmpty(name) && VariableNameChecker.isValid(name);
        boolean nameIsDuplicate = VariableUtils.getVariableNames(variableContainer.getVariables(false, true)).contains(name);
        boolean nameUnchanged = name.equals(variable.getName());
        boolean allowRename = nameValid && !nameIsDuplicate;
        boolean redmineChanged = isRedmineAttribute() && !java.util.Objects.equals(
                Strings.nullToEmpty(redmineFieldName).trim(),
                Strings.nullToEmpty(variable.getRedmineFieldName()).trim());
        boolean allowRedmineOnly = isRename && isRedmineAttribute() && nameValid && nameUnchanged && redmineChanged;
        getButton(IDialogConstants.OK_ID).setEnabled(allowRename || allowRedmineOnly);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("VariableWizard.update.title"));
    }

    public String getName() {
        return name;
    }

    public String getScriptingName() {
        return scriptingName;
    }

    public String getRedmineFieldName() {
        return redmineFieldName == null || redmineFieldName.trim().isEmpty() ? null : redmineFieldName.trim();
    }

}
