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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;

public class EditPropertyDialog extends Dialog {
    private String name = "";
    private String value = "";

    private final boolean updateMode;

    public EditPropertyDialog(Shell parentShell, boolean updateMode) {
        super(parentShell);
        this.updateMode = updateMode;
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
        GridData nameData = new GridData();
        composite.setLayoutData(nameData);

        Label labelName = new Label(composite, SWT.NONE);
        labelName.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelName.setText(Localization.getString("property.name") + ":");
        final Text textName = new Text(composite, SWT.BORDER);
        GridData nameTextData = new GridData(GridData.FILL_HORIZONTAL);
        nameTextData.minimumWidth = 200;
        textName.setText(name);
        textName.setLayoutData(nameTextData);
        textName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                name = textName.getText();
                updateButtons();
            }
        });
        
        Label labelValue = new Label(composite, SWT.NONE);
        labelValue.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelValue.setText(Localization.getString("property.value") + ":");
        final Text textValue = new Text(composite, SWT.BORDER);
        if (updateMode) {
            textName.setEditable(false);
        }
        GridData typeTextData = new GridData(GridData.FILL_HORIZONTAL);
        typeTextData.minimumWidth = 400;
        textValue.setText(value);
        textValue.setLayoutData(typeTextData);
        textValue.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                value = textValue.getText();
                updateButtons();
            }
        });

        return area;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        updateButtons();
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID).setEnabled(name.length() > 0 && value.length() > 0);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString(updateMode ? "property.update.title" : "property.add.title"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
