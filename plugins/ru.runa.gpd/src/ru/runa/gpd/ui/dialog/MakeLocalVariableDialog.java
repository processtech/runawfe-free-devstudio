package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

public class MakeLocalVariableDialog extends Dialog {
    private String name;
    private final ProcessDefinition processDefinition;
    private final Variable variable;
    private Text scriptingNameField;
    private String scriptingName;
    private Button RemoveVariablesButton;
    private boolean RemoveVariables = true;

    public MakeLocalVariableDialog(ProcessDefinition processDefinition, Variable variable) {
        super(Display.getCurrent().getActiveShell());
        this.processDefinition = processDefinition;
        this.name = variable.getName().substring(7);
        this.variable = variable;
        this.scriptingName = variable.getScriptingName().substring(7);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        final Composite composite = new Composite(area, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData());
        Label labelName = new Label(composite, SWT.NONE);
        labelName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelName.setText(Localization.getString("property.name") + ":");
        final Text nameField = new Text(composite, SWT.BORDER);
        GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
        nameTextData.minimumWidth = 200;
        nameField.setText(name);
        nameField.addKeyListener(new SwimlaneNameChecker());
        nameField.setLayoutData(nameTextData);
        nameField.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                name = nameField.getText();
                scriptingName = VariableUtils.generateNameForScripting(processDefinition, name, variable);
                updateButtons();
                scriptingNameField.setText(scriptingName);
            }
        });
        nameField.selectAll();
        new Label(composite, SWT.NONE);
        scriptingNameField = new Text(composite, SWT.BORDER);
        scriptingNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        scriptingNameField.setEditable(false);
        scriptingNameField.setText(scriptingName);
        Text saveAllEditorsLabel = new Text(composite, SWT.MULTI | SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL);
        gridData.horizontalSpan = 2;
        saveAllEditorsLabel.setLayoutData(gridData);
        saveAllEditorsLabel.setText(Localization.getString("warning.allEditorsWillBeSaved"));

        RemoveVariablesButton = new Button(area, SWT.CHECK);
        RemoveVariablesButton.setLayoutData(new GridData());
        RemoveVariablesButton.setText(Localization.getString("MakeLocalWizard.deleteGlobalObject"));
        RemoveVariablesButton.setSelection(true);
        RemoveVariablesButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
                RemoveVariables = RemoveVariablesButton.getSelection();
            }
        });
        return area;
    }

    private void updateButtons() {
        boolean allowCreation = !Strings.isNullOrEmpty(name) && !processDefinition.getVariableNames(true).contains(name)
                && SwimlaneNameChecker.isValid(name, processDefinition);
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("VariableWizard.makeGlobalLocal"));
    }

    public boolean isRemoveVariables() {
        return RemoveVariables;
    }

    public String getName() {
        return name;
    }

    public String getScriptingName() {
        return scriptingName;
    }

}
