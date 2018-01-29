package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
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
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.Duration.Unit;

import com.google.common.base.Strings;

public class DurationEditDialog extends Dialog {
    private static final int CLEAR_ID = 111;
    private final ProcessDefinition processDefinition;
    private Duration editable;
    private Text baseDateField;
    private Text delayField;
    private Text unitField;

    public DurationEditDialog(ProcessDefinition definition, String duration) {
        super(Display.getCurrent().getActiveShell());
        this.processDefinition = definition;
        if (!Strings.isNullOrEmpty(duration)) {
            editable = new Duration(duration);
        } else {
            editable = new Duration();
        }
    }

    public DurationEditDialog(ProcessDefinition processDefinition, Duration duration) {
        this(processDefinition, duration.getDuration());
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(2, false));
        {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData gridData = new GridData();
            gridData.horizontalSpan = 2;
            label.setLayoutData(gridData);
            label.setText(Localization.getString("property.duration.baseDate"));
        }
        {
            baseDateField = new Text(area, SWT.BORDER);
            baseDateField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            baseDateField.setLayoutData(gridData);
            SWTUtils.createButton(area, "...", new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    ChooseDurationDialog dialog = new ChooseDurationDialog(processDefinition);
                    String result = dialog.openDialog();
                    if (result != null) {
                        editable.setVariableName(result);
                        updateGUI();
                    }
                }
            }).setEnabled(processDefinition != null);
        }
        {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setText(Localization.getString("property.duration.delay"));
        }
        {
            delayField = new Text(area, SWT.MULTI | SWT.BORDER);
            delayField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            gridData.minimumHeight = 200;
            delayField.setLayoutData(gridData);
            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            gridData = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
            button.setLayoutData(gridData);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    LongInputDialog inputDialog = new LongInputDialog();
                    inputDialog.setInitialValue(editable.getDelay().replaceAll(" ", ""));
                    if (inputDialog.open() == IDialogConstants.OK_ID) {
                        editable.setDelay(inputDialog.getUserInput());
                        updateGUI();
                    }
                }
            });
        }
        {
            final Label label = new Label(area, SWT.NO_BACKGROUND);
            GridData data = new GridData();
            data.horizontalSpan = 2;
            label.setLayoutData(data);
            label.setText(Localization.getString("property.duration.format"));
            unitField = new Text(area, SWT.BORDER);
            unitField.setEditable(false);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.minimumWidth = 200;
            unitField.setLayoutData(gridData);
            Button button = new Button(area, SWT.PUSH);
            button.setText("...");
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ChooseItemDialog<Unit> dialog = new ChooseItemDialog<Unit>(label.getText(), Duration.getUnits(), false, null, false);
                    Unit unit = dialog.openDialog();
                    if (unit != null) {
                        editable.setUnit(unit);
                        updateGUI();
                    }
                }
            });
        }
        return area;
    }

    private void updateGUI() {
        if (editable.getVariableName() != null) {
            baseDateField.setText(editable.getVariableName());
        } else {
            baseDateField.setText(Duration.CURRENT_DATE_MESSAGE);
        }
        delayField.setText(editable.getDelay());
        unitField.setText(editable.getUnit().toString());
        boolean valid = false;
        try {
            new Duration(editable.getDuration());
            valid = true;
        } catch (Throwable th) {
        }
        getButton(IDialogConstants.OK_ID).setEnabled(valid);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("property.duration"));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        Button button = createButton(parent, CLEAR_ID, Localization.getString("button.clear"), false);
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editable = new Duration();
                updateGUI();
            }
        });
        updateGUI();
    }

    public Object openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return editable;
        }
        return null;
    }
}
