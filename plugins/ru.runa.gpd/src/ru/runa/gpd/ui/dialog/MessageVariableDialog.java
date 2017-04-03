package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.JavaIdentifierChecker;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableMapping;

public class MessageVariableDialog extends Dialog {
    private final List<String> variableNames;
    private final boolean usageSelector;
    private final VariableMapping oldMapping;
    private String variable = "";
    private String alias = "";
    private Text aliasText;

    protected MessageVariableDialog(List<String> variableNames, boolean usageSelector, VariableMapping oldMapping) {
        super(Display.getCurrent().getActiveShell());
        this.variableNames = variableNames;
        this.usageSelector = usageSelector;
        this.oldMapping = oldMapping;
        if (oldMapping != null) {
            this.variable = oldMapping.getName();
            this.alias = oldMapping.getMappedName();
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String message = oldMapping != null ? Localization.getString("button.update") : Localization.getString("button.create");
        newShell.setText(message);
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

        Label labelProcessVariable = new Label(composite, SWT.NONE);
        labelProcessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelProcessVariable.setText(Localization.getString(usageSelector ? "property.name" : "MessageNodeDialog.VariableName") + ":");
        if (usageSelector) {
            final Text variableText = new Text(composite, SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            variableText.setLayoutData(gridData);
            variableText.setText(getVariable());
            variableText.addKeyListener(new JavaIdentifierChecker());
            variableText.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    variable = variableText.getText();
                }

            });
        } else {
            final Combo variableCombo = new Combo(composite, SWT.BORDER);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            variableCombo.setItems(variableNames.toArray(new String[variableNames.size()]));
            variableCombo.setLayoutData(gridData);
            variableCombo.setText(getVariable());
            variableCombo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    variable = variableCombo.getText();
                    aliasText.setText(variable);
                }
            });
        }

        Label labelSubprocessVariable = new Label(composite, SWT.NONE);
        labelSubprocessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelSubprocessVariable.setText(Localization.getString(usageSelector ? "property.value" : "MessageNodeDialog.Alias") + ":");
        aliasText = new Text(composite, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 200;
        aliasText.setLayoutData(gridData);
        aliasText.setText(getAlias());

        aliasText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                alias = aliasText.getText();
            }
        });
        if (usageSelector) {
            new InsertVariableTextMenuDetectListener(aliasText, variableNames);
        }

        return area;
    }

    public String getVariable() {
        return variable;
    }

    public String getAlias() {
        return alias;
    }
}
