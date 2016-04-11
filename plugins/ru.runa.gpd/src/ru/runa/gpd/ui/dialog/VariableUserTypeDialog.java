package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.VariableNameChecker;

public class VariableUserTypeDialog extends Dialog {
    private String name;
    private final ProcessDefinition processDefinition;
    private final boolean createMode;

    public VariableUserTypeDialog(ProcessDefinition processDefinition, VariableUserType type) {
        super(Display.getCurrent().getActiveShell());
        this.processDefinition = processDefinition;
        this.name = type != null ? type.getName() : "";
        this.createMode = type == null;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString(createMode ? "UserDefinedVariableType.create.title" : "UserDefinedVariableType.update.title"));
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
        if (!createMode) {
            nameField.selectAll();
        }
        return area;
    }

    private void updateButtons() {
    	VariableUserType type = processDefinition.getVariableUserType(name);
        boolean allowCreation = type == null && VariableFormatRegistry.getInstance().getArtifactByLabel(name) == null;
        getButton(IDialogConstants.OK_ID).setEnabled(allowCreation);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    public String getName() {
        return name;
    }

}
