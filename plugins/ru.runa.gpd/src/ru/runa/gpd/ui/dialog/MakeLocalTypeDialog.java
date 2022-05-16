package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.VariableNameChecker;

public class MakeLocalTypeDialog extends Dialog {
    private String name;
    private final ProcessDefinition processDefinition;
    private Button RemoveTypeButton;
    private boolean RemoveTypes = true;

    public MakeLocalTypeDialog(ProcessDefinition processDefinition, VariableUserType type) {
        super(Display.getCurrent().getActiveShell());
        this.processDefinition = processDefinition;
        this.name = type.getName().substring(7);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);

        Composite composite = new Composite(area, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
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
        nameField.addKeyListener(new VariableNameChecker());
        nameField.setLayoutData(nameTextData);
        nameField.addModifyListener(new LoggingModifyTextAdapter() {

            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                name = nameField.getText();
                updateButtons();
            }
        });
        nameField.selectAll();
        RemoveTypeButton = new Button(area, SWT.CHECK);
        RemoveTypeButton.setLayoutData(new GridData());
        RemoveTypeButton.setText(Localization.getString("MakeLocalWizard.deleteGlobalObject"));
        RemoveTypeButton.setSelection(true);
        RemoveTypeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons();
                RemoveTypes = RemoveTypeButton.getSelection();
            }
        });

        return area;
    }

    private void updateButtons() {
        VariableUserType type = processDefinition.getVariableUserType(name);
        boolean allowCreation = type == null && VariableFormatRegistry.getInstance().getArtifactByLabel(name) == null
                && VariableNameChecker.isValid(name);
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    public String getName() {
        return name;
    }

    public boolean isRemoveTypes() {
        return RemoveTypes;
    }
}
