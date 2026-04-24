package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
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
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.Duration.Unit;

public class IntervalEditDialog extends Dialog {
    private static final int CLEAR_ID = 111;
    private Duration editable;
    private Text delayField;
    private Text unitField;

    public IntervalEditDialog(Duration duration) {
        super(Display.getCurrent().getActiveShell());
        this.editable = duration != null ? new Duration(duration) : new Duration();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(2, false));

        delayField = createEditorField(
                area,
                Localization.getString("property.interval.delay"),
                this::openDelayDialog
        );

        unitField = createEditorField(
                area,
                Localization.getString("property.interval.format"),
                this::openUnitDialog
        );

        updateGUI();
        return area;
    }

    private Text createEditorField(Composite parent, String labelText, Runnable action) {

        Label label = new Label(parent, SWT.NONE);
        GridData labelData = new GridData();
        labelData.horizontalSpan = 2;
        label.setLayoutData(labelData);
        label.setText(labelText);

        Text field = new Text(parent, SWT.BORDER);
        field.setEditable(false);
        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button button = new Button(parent, SWT.PUSH);
        button.setText("...");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                action.run();
            }
        });

        return field;
    }

    private void openDelayDialog() {
        LongInputDialog dialog = new LongInputDialog();
        dialog.setInitialValue(normalizeDelay(editable.getDelay()));

        if (dialog.open() == IDialogConstants.OK_ID) {
            editable.setDelay(dialog.getUserInput());
            updateGUI();
        }
    }

    private void openUnitDialog() {
        ChooseItemDialog<Unit> dialog =
                new ChooseItemDialog<>(
                        Localization.getString("property.interval.format"),
                        Duration.getUnits(),
                        false,
                        null,
                        false);

        Unit unit = dialog.openDialog();

        if (unit != null) {
            editable.setUnit(unit);
            updateGUI();
        }
    }

    private String normalizeDelay(String delay) {
        if (Strings.isNullOrEmpty(delay)) {
            return "0";
        }
        return delay.replace(" ", "");
    }

    private void updateGUI() {

        delayField.setText(
                Strings.isNullOrEmpty(editable.getDelay()) ? "0" : editable.getDelay()
        );

        unitField.setText(
                editable.getUnit() != null ? editable.getUnit().toString() : ""
        );

        Button ok = getButton(IDialogConstants.OK_ID);
        if (ok != null) {
            ok.setEnabled(editable.hasDuration());
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("property.interval.title"));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {

        Button clearButton = createButton(parent, CLEAR_ID, Localization.getString("button.clear"), false);

        super.createButtonsForButtonBar(parent);

        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                editable = new Duration();
                updateGUI();
            }
        });
    }

    public Duration openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            editable.setVariableName(null);
            return editable;
        }
        return null;
    }
}