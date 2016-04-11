package ru.runa.gpd.extension;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;

public class DelegableConfigurationDialog extends Dialog {
    protected StyledText styledText;
    protected String title;
    protected final String initialValue;
    protected String result;

    public DelegableConfigurationDialog(String initialValue) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.title = Localization.getString("property.delegation.configuration");
        this.initialValue = initialValue;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createDialogHeader(composite);
        
        styledText = new StyledText(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        styledText.setLayoutData(new GridData(GridData.FILL_BOTH));
        styledText.setLineSpacing(2);

        createDialogFooter(composite);
        styledText.setText(this.initialValue);
        styledText.setFocus();
        return composite;
    }
    
    protected void createDialogHeader(Composite composite) {
    }

    protected void createDialogFooter(Composite composite) {
    }

    @Override
    protected void okPressed() {
        this.result = styledText.getText();
        super.okPressed();
    }

    public String getResult() {
        return result;
    }

}
