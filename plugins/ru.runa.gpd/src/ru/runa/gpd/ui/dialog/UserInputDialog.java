package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.ui.custom.TypedUserInputCombo;

public class UserInputDialog extends Dialog implements Listener, FocusListener {
    private final String title;
    protected Label label;
    private Text text;
    private String value;

    public UserInputDialog(String title) {
        super(Display.getCurrent().getActiveShell());
        this.title = title;
    }

    public UserInputDialog() {
        this(TypedUserInputCombo.INPUT_VALUE);
    }

    public void setInitialValue(String initialValue) {
        this.value = initialValue;
        if (text != null && !text.isDisposed()) {
            text.setText(value);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (value != null) {
            text.setText(value);
        }
        text.addListener(SWT.Modify, this);
        text.addFocusListener(this);
        postCreation();
        return composite;
    }

    protected void postCreation() {
    }

    @Override
    public void handleEvent(Event event) {
        String newValue = text.getText();
        if (validate(newValue)) {
            value = newValue;
            getButton(IDialogConstants.OK_ID).setEnabled(true);
        } else {
            getButton(IDialogConstants.OK_ID).setEnabled(false);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    @Override
    public void focusLost(FocusEvent e) {
    }

    protected boolean validate(String newValue) {
        return newValue.length() > 0;
    }

    public String getUserInput() {
        return value;
    }
}
