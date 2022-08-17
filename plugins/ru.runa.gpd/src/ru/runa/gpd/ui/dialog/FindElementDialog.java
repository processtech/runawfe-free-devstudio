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

import ru.runa.gpd.Localization;

public class FindElementDialog extends Dialog implements Listener, FocusListener {
    private String title;
    protected Label labelID;
    private Text textID;
    private String valueID;
    protected Label labelName;
    private Text textName;
    private String valueName;

    public FindElementDialog() {
        super(Display.getCurrent().getActiveShell());
        this.title = Localization.getString("InputElement");        
    }
    
    public void setInitialValue(String initialValueID, String initialValueName) {
        this.valueID = initialValueID;
        if (textID != null && !textID.isDisposed()) {
            textID.setText(valueID);
        }
        
        this.valueName = initialValueName;
        if (textName != null && !textName.isDisposed()) {
            textName.setText(valueName);
        }
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, true));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        labelID = new Label(composite, SWT.SINGLE);
        labelID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelID.setText(Localization.getString("InputElementID"));

        textID = new Text(composite, SWT.SINGLE | SWT.BORDER);
        textID.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (valueID != null) {
            textID.setText(valueID);
        }
        textID.addListener(SWT.Modify, this);
        textID.addFocusListener(this);
        
        labelName = new Label(composite, SWT.SINGLE);
        labelName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        labelName.setText(Localization.getString("InputElementName"));

        textName = new Text(composite, SWT.SINGLE | SWT.BORDER);
        textName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (valueName != null) {
            textName.setText(valueName);
        }
        textName.addListener(SWT.Modify, this);
        textName.addFocusListener(this);
        
        postCreation();
        return composite;
    }
    
    protected void postCreation() {
    }

    @Override
    public void handleEvent(Event event) {
        String newValueID = textID.getText();
        String newValueName = textName.getText();
        if (validate(newValueID, newValueName)) {
            valueID = newValueID;
            valueName = newValueName;
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
    
    protected boolean validate(String newValueID, String newValueName) {
        return (newValueID != null && newValueID.trim().length() > 0) || (newValueName != null && newValueName.trim().length() > 0);
    }
    
    public String getUserInputID() {       
        if (valueID == null) {
            return null;
        } else {
            if (valueID.trim().length() == 0) {
                return null;
            } else {
                return valueID.trim().toLowerCase();
            }
        }
    }
    
    public String getUserInputName() {
        if (valueName == null) {
            return null;
        } else {
            if (valueName.trim().length() == 0) {
                return null;
            } else {
                return valueName.trim().toLowerCase();
            }
        }
    }
}
