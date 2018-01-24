package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.VariableMapping;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SubprocessVariableDialog extends Dialog {
    private final List<String> processVariables;
    private final List<String> subprocessVariables;
    private String processVariable = "variable";
    private String subprocessVariable = "variable";
    private final List<String> usages = Lists.newArrayList();
    private final VariableMapping oldMapping;

    protected SubprocessVariableDialog(List<String> processVariables, List<String> subprocessVariables, VariableMapping oldMapping) {
        super(Display.getCurrent().getActiveShell());
        this.processVariables = processVariables;
        this.subprocessVariables = subprocessVariables;
        this.oldMapping = oldMapping;
        if (oldMapping != null) {
            this.processVariable = oldMapping.getName();
            this.subprocessVariable = oldMapping.getMappedName();
            if (oldMapping.isReadable()) {
                this.usages.add(VariableMapping.USAGE_READ);
            }
            if (oldMapping.isWritable()) {
                this.usages.add(VariableMapping.USAGE_WRITE);
            }
            if (oldMapping.isSyncable()) {
                this.usages.add(VariableMapping.USAGE_SYNC);
            }
        } else {
            this.usages.add(VariableMapping.USAGE_READ);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        String message = oldMapping != null ? Localization.getString("Subprocess.UpdateVariableMapping") : Localization
                .getString("Subprocess.CreateVariableMapping");
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
        labelProcessVariable.setText(Localization.getString("Subprocess.ProcessVariableName") + ":");

        Composite varComposite1 = new Composite(composite, SWT.NONE);
        GridData processVariableTextData = new GridData(GridData.FILL_HORIZONTAL);
        processVariableTextData.minimumWidth = 200;
        varComposite1.setLayoutData(processVariableTextData);
        varComposite1.setLayout(new GridLayout(2, false));
        final Text varName1 = new Text(varComposite1, SWT.READ_ONLY | SWT.BORDER);
        varName1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        varName1.setText(getProcessVariable());
        Button selectButton = new Button(varComposite1, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        selectButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = new ChooseVariableNameDialog(processVariables).openDialog();
                if (result != null) {
                    processVariable = result;
                    varName1.setText(processVariable);
                }
            }
        });

        Label labelSubprocessVariable = new Label(composite, SWT.NONE);
        labelSubprocessVariable.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        labelSubprocessVariable.setText(Localization.getString("Subprocess.SubprocessVariableName") + ":");

        Composite varComposite2 = new Composite(composite, SWT.NONE);
        varComposite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        varComposite2.setLayout(new GridLayout(2, false));
        final Text varName2 = new Text(varComposite2, SWT.READ_ONLY | SWT.BORDER);
        varName2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        varName2.setText(getSubprocessVariable());
        Button selectButton2 = new Button(varComposite2, SWT.PUSH);
        selectButton2.setText("...");
        selectButton2.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        selectButton2.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String result = new ChooseVariableNameDialog(subprocessVariables).openDialog();
                if (result != null) {
                    subprocessVariable = result;
                    varName2.setText(subprocessVariable);
                }
            }
        });

        Group g = new Group(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        g.setLayoutData(gd);
        g.setLayout(new GridLayout(1, false));
        g.setText(Localization.getString("VariableMapping.Usage"));
        final Button readCheckbox = new Button(g, SWT.CHECK);
        readCheckbox.setText(Localization.getString("VariableMapping.Usage.Read"));
        readCheckbox.setSelection(usages.contains(VariableMapping.USAGE_READ));
        final Button writeCheckbox = new Button(g, SWT.CHECK);
        writeCheckbox.setText(Localization.getString("VariableMapping.Usage.Write"));
        writeCheckbox.setSelection(usages.contains(VariableMapping.USAGE_WRITE));
        final Button syncCheckbox = new Button(g, SWT.CHECK);
        syncCheckbox.setText(Localization.getString("VariableMapping.Usage.Sync"));
        syncCheckbox.setSelection(usages.contains(VariableMapping.USAGE_SYNC));
        readCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons(readCheckbox.getSelection(), VariableMapping.USAGE_READ);
            }
        });
        writeCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons(writeCheckbox.getSelection(), VariableMapping.USAGE_WRITE);
            }
        });
        syncCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                updateButtons(syncCheckbox.getSelection(), VariableMapping.USAGE_SYNC);
            }
        });
        return area;
    }

    private void updateButtons(boolean selected, String usage) {
        if (selected) {
            usages.add(usage);
        } else {
            usages.remove(usage);
        }
        getButton(OK).setEnabled(!usages.isEmpty());
    }

    public String getAccess() {
        return Joiner.on(",").join(usages);
    }

    public String getProcessVariable() {
        return processVariable;
    }

    public String getSubprocessVariable() {
        return subprocessVariable;
    }
}
